package com.biblioshare.pret.service;


import static com.biblioshare.pret.entity.Loan.Status.*;

import com.biblioshare.pret.client.*;
import com.biblioshare.pret.dto.*;
import com.biblioshare.pret.dto.RemoteData.*;
import com.biblioshare.pret.entity.*;
import com.biblioshare.pret.repository.*;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoanService {
    private final LoanRepository loans;
    private final FineRepository fines;
    private final CatalogueClient catalogue;
    private final AdherentsClient adherents;
    private final RemoteReads reads;
    private final TransactionTemplate transactions;
    private final ApplicationEventPublisher events;
    private final RateLimiterRegistry limits;

    public LoanService(LoanRepository loans, FineRepository fines, CatalogueClient catalogue,
                       AdherentsClient adherents, RemoteReads reads, TransactionTemplate transactions,
                       ApplicationEventPublisher events, RateLimiterRegistry limits) {
        this.loans = loans;
        this.fines = fines;
        this.catalogue = catalogue;
        this.adherents = adherents;
        this.reads = reads;
        this.transactions = transactions;
        this.events = events;
        this.limits = limits;
    }

    public record Creation(Loan loan, boolean created) {}
    public record LateNotice(Long loanId, String email) {}

    public List<Loan> all() {
        return loans.findAll();
    }

    public Loan get(long id) {
        return loans.findById(id).orElseThrow(() ->
                error(HttpStatus.NOT_FOUND, "Emprunt introuvable"));
    }

    public synchronized Creation create(String key, LoanRequest request) {
        if (key.isBlank() || key.length() > 128) {
            throw error(HttpStatus.BAD_REQUEST, "Idempotency-Key invalide");
        }

        Loan loan = loans.findByIdempotencyKey(key).orElse(null);
        if (loan != null) {
            if (!loan.copyId.equals(request.copyId()) || !loan.memberId.equals(request.memberId())
                    || !loan.dateRetourPrevue.equals(request.dateRetourPrevue())) {
                throw error(HttpStatus.CONFLICT, "Cle deja associee a une autre demande");
            }
            if (loan.statut != EN_ATTENTE) {
                return new Creation(loan, false);
            }
            requireEligible(loan.memberId);
        } else {
            if (request.dateRetourPrevue().isBefore(LocalDate.now())) {
                throw error(HttpStatus.BAD_REQUEST, "La date prevue precede la date d'emprunt");
            }
            Copy copy = reads.copy(request.copyId());
            if (!copy.disponible()) {
                throw error(HttpStatus.CONFLICT, "Exemplaire indisponible");
            }
            requireEligible(request.memberId());
            loan = new Loan();
            loan.copyId = request.copyId();
            loan.memberId = request.memberId();
            loan.dateEmprunt = LocalDate.now();
            loan.dateRetourPrevue = request.dateRetourPrevue();
            loan.statut = EN_ATTENTE;
            loan.idempotencyKey = key;
            loan.claimToken = UUID.randomUUID();
            loan.bookId = copy.bookId();
            loan = loans.saveAndFlush(loan);
        }

        // Keep the pending row on network failure so replay can reuse the same claim.
        Copy claimed;
        try {
            claimed = catalogue.claim(loan.copyId, loan.claimToken);
        } catch (FeignException.Conflict | FeignException.NotFound failure) {
            loans.delete(loan);
            throw failure;
        }
        loan.bookId = claimed.bookId();
        loan.statut = EN_COURS;
        return new Creation(loans.saveAndFlush(loan), true);
    }

    public LoanDetails details(long id) {
        Loan loan = get(id);
        if (!limits.rateLimiter("consultation").acquirePermission()) {
            return new LoanDetails(loan, null, null, null, fines.findAllByLoanId(id),
                    List.of("copy", "book", "member"));
        }
        List<String> missing = new ArrayList<>();
        Copy copy = optional("copy", missing, () -> reads.copy(loan.copyId));
        Long bookId = copy == null ? loan.bookId : copy.bookId();
        Book book = optional("book", missing, () -> reads.book(bookId));
        Member member = optional("member", missing, () -> reads.member(loan.memberId));
        return new LoanDetails(loan, copy, book, member, fines.findAllByLoanId(id), missing);
    }

    public synchronized Loan returnCopy(long id) {
        Loan loan = get(id);
        if (loan.statut == EN_COURS || loan.statut == EN_RETARD) {
            registerLateIfNeeded(loan);
            catalogue.release(loan.copyId, loan.claimToken);
            loan.dateRetourReelle = LocalDate.now();
            loan.statut = RETOURNE;
            loans.saveAndFlush(loan);
        } else if (loan.statut != RETOURNE) {
            throw error(HttpStatus.CONFLICT, "Retour impossible dans cet etat");
        }
        if (!loan.disponibiliteTraitee) {
            adherents.available(loan.claimToken, loan.bookId);
            loan.disponibiliteTraitee = true;
            loans.saveAndFlush(loan);
        }
        return loan;
    }

    public synchronized Loan cancel(long id) {
        Loan loan = get(id);
        if (loan.statut == ANNULE) {
            return loan;
        }
        if (loan.statut != EN_ATTENTE) {
            throw error(HttpStatus.CONFLICT, "Seul un emprunt en attente peut etre annule");
        }
        try {
            catalogue.release(loan.copyId, loan.claimToken);
        } catch (FeignException.NotFound alreadyRemoved) {
            // No allocation remains if the copy no longer exists.
        }
        loan.statut = ANNULE;
        return loans.saveAndFlush(loan);
    }

    public synchronized Loan markLost(long id) {
        Loan loan = get(id);
        if (loan.statut == PERDU) {
            return loan;
        }
        if (loan.statut != EN_COURS && loan.statut != EN_RETARD) {
            throw error(HttpStatus.CONFLICT, "Emprunt inactif");
        }
        registerLateIfNeeded(loan);
        loan.statut = PERDU;
        return loans.saveAndFlush(loan);
    }

    public synchronized int detectLateLoans() {
        List<Loan> overdue = loans.findByStatutAndDateRetourPrevueBefore(EN_COURS, LocalDate.now());
        for (Loan loan : overdue) {
            registerLateIfNeeded(loan);
        }
        return overdue.size();
    }

    public List<Fine> fines() {
        return fines.findAll();
    }

    public synchronized Fine pay(long id) {
        return transactions.execute(status -> {
            Fine fine = fines.findById(id).orElseThrow(() ->
                    error(HttpStatus.NOT_FOUND, "Amende introuvable"));
            fine.statut = Fine.Status.PAYEE;
            fines.saveAndFlush(fine);
            Long memberId = get(fine.loanId).memberId;
            adherents.suspend(memberId, hasUnpaidFines(memberId));
            return fine;
        });
    }

    private void registerLateIfNeeded(Loan loan) {
        if (loan.statut != EN_COURS || !loan.dateRetourPrevue.isBefore(LocalDate.now())) {
            return;
        }
        Member member = reads.member(loan.memberId);
        transactions.executeWithoutResult(status -> {
            loan.statut = EN_RETARD;
            loans.save(loan);
            if (!fines.existsByLoanId(loan.id)) {
                Fine fine = new Fine();
                fine.loanId = loan.id;
                fine.montant = new BigDecimal("10.00");
                fine.date = LocalDate.now();
                fine.statut = Fine.Status.IMPAYEE;
                fines.saveAndFlush(fine);
                adherents.suspend(loan.memberId, true);
                events.publishEvent(new LateNotice(loan.id, member.email()));
            }
        });
    }

    private void requireEligible(Long memberId) {
        if (reads.member(memberId).suspendu() || hasUnpaidFines(memberId)) {
            throw error(HttpStatus.CONFLICT, "Adherent suspendu ou amende impayee");
        }
    }

    private boolean hasUnpaidFines(Long memberId) {
        return fines.existsForMember(memberId, Fine.Status.IMPAYEE);
    }

    private static <T> T optional(String field, List<String> missing, Supplier<T> read) {
        try {
            return read.get();
        } catch (RemoteUnavailable | CallNotPermittedException | FeignException.NotFound failure) {
            missing.add(field);
            return null;
        }
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }
}
