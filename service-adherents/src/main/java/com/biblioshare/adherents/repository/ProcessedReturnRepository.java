package com.biblioshare.adherents.repository;


import com.biblioshare.adherents.entity.ProcessedReturn;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedReturnRepository extends JpaRepository<ProcessedReturn, UUID> {}
