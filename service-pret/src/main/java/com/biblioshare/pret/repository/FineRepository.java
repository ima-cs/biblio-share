package com.biblioshare.pret.repository;


import com.biblioshare.pret.entity.Fine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findAllByLoanId(Long loanId);
    boolean existsByLoanId(Long loanId);

    @Query("""
            select count(f) > 0 from Fine f, Loan l
            where f.loanId = l.id and l.memberId = :memberId and f.statut = :statut
            """)
    boolean existsForMember(Long memberId, Fine.Status statut);
}
