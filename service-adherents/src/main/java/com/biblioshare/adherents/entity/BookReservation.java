package com.biblioshare.adherents.entity;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class BookReservation {
    public enum Status { EN_ATTENTE, NOTIFIEE, ANNULEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long memberId;
    public Long bookId;
    public Instant date;
    @Enumerated(EnumType.STRING)
    public Status statut;
}
