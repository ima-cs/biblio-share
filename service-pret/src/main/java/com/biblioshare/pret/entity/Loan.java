package com.biblioshare.pret.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Loan {
    public enum Status { EN_ATTENTE, EN_COURS, RETOURNE, EN_RETARD, PERDU, ANNULE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long copyId;
    public Long memberId;
    public LocalDate dateEmprunt;
    public LocalDate dateRetourPrevue;
    public LocalDate dateRetourReelle;
    @Enumerated(EnumType.STRING)
    public Status statut;
    @Column(nullable = false, unique = true, length = 128)
    public String idempotencyKey;
    @JsonIgnore
    @Column(nullable = false, unique = true)
    public UUID claimToken;
    @JsonIgnore
    public Long bookId;
    @JsonIgnore
    public boolean disponibiliteTraitee;
}
