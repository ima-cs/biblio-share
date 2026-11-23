package com.biblioshare.adherents.repository;


import com.biblioshare.adherents.entity.BookReservation;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<BookReservation, Long> {
    boolean existsByMemberIdAndStatut(Long memberId, BookReservation.Status statut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookReservation> findFirstByBookIdAndStatutOrderByDateAscIdAsc(
            Long bookId, BookReservation.Status statut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BookReservation r where r.id = :id")
    Optional<BookReservation> lockById(Long id);
}
