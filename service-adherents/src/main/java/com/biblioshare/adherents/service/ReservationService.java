package com.biblioshare.adherents.service;


import static com.biblioshare.adherents.entity.BookReservation.Status.*;

import com.biblioshare.adherents.client.CatalogueBooks;
import feign.FeignException;
import com.biblioshare.adherents.dto.ReservationRequest;
import com.biblioshare.adherents.entity.*;
import com.biblioshare.adherents.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservations;
    private final ProcessedReturnRepository returns;
    private final MemberService members;
    private final CatalogueBooks catalogue;
    private final ApplicationEventPublisher events;

    public ReservationService(ReservationRepository reservations, ProcessedReturnRepository returns,
                              MemberService members, CatalogueBooks catalogue, ApplicationEventPublisher events) {
        this.reservations = reservations;
        this.returns = returns;
        this.members = members;
        this.catalogue = catalogue;
        this.events = events;
    }

    public record AvailabilityNotice(Long reservationId, Long bookId, String email) {}

    public List<BookReservation> all() {
        return reservations.findAll();
    }

    public BookReservation get(long id) {
        return reservations.findById(id).orElseThrow(ReservationService::notFound);
    }

    public BookReservation create(ReservationRequest request) {
        BookReservation reservation = new BookReservation();
        reservation.date = Instant.now();
        reservation.statut = EN_ATTENTE;
        apply(reservation, request);
        return reservations.save(reservation);
    }

    public BookReservation update(long id, ReservationRequest request) {
        BookReservation reservation = waiting(id);
        apply(reservation, request);
        return reservation;
    }

    public BookReservation cancel(long id) {
        BookReservation reservation = waiting(id);
        reservation.statut = ANNULEE;
        return reservation;
    }

    public void delete(long id) {
        reservations.delete(get(id));
    }

    public void available(UUID token, Long bookId) {
        if (returns.existsById(token)) {
            return;
        }
        // Persist the receipt even if nobody is waiting.
        returns.saveAndFlush(new ProcessedReturn(token));
        reservations.findFirstByBookIdAndStatutOrderByDateAscIdAsc(bookId, EN_ATTENTE)
                .ifPresent(reservation -> {
                    Member member = members.get(reservation.memberId);
                    reservation.statut = NOTIFIEE;
                    events.publishEvent(new AvailabilityNotice(reservation.id, bookId, member.email));
                });
    }

    private void apply(BookReservation reservation, ReservationRequest request) {
        members.get(request.memberId());
        try {
            catalogue.book(request.bookId());
        } catch (FeignException.NotFound failure) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livre introuvable");
        } catch (FeignException failure) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogue indisponible");
        }
        reservation.memberId = request.memberId();
        reservation.bookId = request.bookId();
    }

    private BookReservation waiting(long id) {
        BookReservation reservation = reservations.lockById(id).orElseThrow(ReservationService::notFound);
        if (reservation.statut != EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation deja traitee");
        }
        return reservation;
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation introuvable");
    }
}
