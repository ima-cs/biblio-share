package com.biblioshare.pret;


import com.biblioshare.pret.client.*;
import com.biblioshare.pret.dto.LoanRequest;
import com.biblioshare.pret.dto.RemoteData.*;
import com.biblioshare.pret.entity.Fine;
import com.biblioshare.pret.entity.Loan;
import com.biblioshare.pret.repository.LoanRepository;
import com.biblioshare.pret.service.LoanService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class LoanFlowTests {
    @Autowired LoanService service;
    @Autowired LoanRepository loans;
    @MockitoBean CatalogueClient catalogue;
    @MockitoBean AdherentsClient adherents;
    @MockitoBean RabbitTemplate rabbit;

    @Test
    void replayOverduePaymentAndReturn() {
        var available = new Copy(1L, 2L, "EX-1", "BON", true);
        var taken = new Copy(1L, 2L, "EX-1", "BON", false);
        when(catalogue.copy(1L)).thenReturn(available);
        when(catalogue.claim(eq(1L), any())).thenReturn(taken);
        when(adherents.member(3L)).thenReturn(new Member(3L, "Salma", "salma@example.test", "ETUDIANT", false));

        var request = new LoanRequest(1L, 3L, LocalDate.now());
        var creation = service.create("loan-flow", request);
        assertThat(creation.created()).isTrue();
        Loan loan = creation.loan();
        var replay = service.create("loan-flow", request);
        assertThat(replay.created()).isFalse();
        assertThat(replay.loan().id).isEqualTo(loan.id);
        assertThat(loans.count()).isEqualTo(1);
        verify(catalogue, times(1)).claim(1L, loan.claimToken);

        loan.dateRetourPrevue = LocalDate.now().minusDays(1);
        loans.saveAndFlush(loan);
        assertThat(service.detectLateLoans()).isEqualTo(1);
        assertThat(service.detectLateLoans()).isZero();
        assertThat(service.get(loan.id).statut).isEqualTo(Loan.Status.EN_RETARD);
        assertThat(service.fines()).hasSize(1);
        verify(adherents).suspend(3L, true);
        verify(rabbit).send(eq(""), eq("notifications.email"), any(org.springframework.amqp.core.Message.class));

        var fine = service.pay(service.fines().get(0).id);
        assertThat(fine.statut).isEqualTo(Fine.Status.PAYEE);
        verify(adherents).suspend(3L, false);
        assertThat(service.returnCopy(loan.id).statut).isEqualTo(Loan.Status.RETOURNE);
        assertThat(service.returnCopy(loan.id).statut).isEqualTo(Loan.Status.RETOURNE);
        verify(catalogue).release(1L, loan.claimToken);
        verify(adherents).available(loan.claimToken, 2L);
    }
}
