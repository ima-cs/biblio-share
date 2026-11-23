package com.biblioshare.pret.repository;


import com.biblioshare.pret.entity.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByIdempotencyKey(String key);
    List<Loan> findByStatutAndDateRetourPrevueBefore(Loan.Status statut, LocalDate date);
}
