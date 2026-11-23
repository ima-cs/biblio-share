package com.biblioshare.pret.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Fine {
    public enum Status { IMPAYEE, PAYEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public Long loanId;
    public BigDecimal montant;
    public LocalDate date;
    @Enumerated(EnumType.STRING)
    public Status statut;
}
