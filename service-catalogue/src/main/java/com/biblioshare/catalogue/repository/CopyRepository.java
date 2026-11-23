package com.biblioshare.catalogue.repository;

import com.biblioshare.catalogue.entity.Copy;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CopyRepository extends JpaRepository<Copy, Long> {
    boolean existsByBookId(Long bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Copy c where c.id = :id")
    Optional<Copy> lockById(Long id);
}
