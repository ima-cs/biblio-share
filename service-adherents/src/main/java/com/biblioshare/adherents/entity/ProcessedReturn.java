package com.biblioshare.adherents.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class ProcessedReturn {
    @Id
    private UUID token;

    protected ProcessedReturn() {}

    public ProcessedReturn(UUID token) {
        this.token = token;
    }
}
