package com.biblioshare.catalogue.entity;

import jakarta.persistence.*;

@Entity
public class Copy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private String codeExemplaire;
    private String etat;
    private boolean disponible = true;

    private String empruntToken;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isAttribue() {
        return empruntToken != null;
    }

    public boolean estAttribueA(String token) {
        return token.equals(empruntToken);
    }

    public void attribuer(String token) {
        empruntToken = token;
        disponible = false;
    }

    public void liberer() {
        empruntToken = null;
        disponible = true;
    }

    public Copy() {
    }

    public Copy(Long bookId, String codeExemplaire, String etat, boolean disponible) {
        this.bookId = bookId;
        this.codeExemplaire = codeExemplaire;
        this.etat = etat;
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getCodeExemplaire() {
        return codeExemplaire;
    }

    public void setCodeExemplaire(String codeExemplaire) {
        this.codeExemplaire = codeExemplaire;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
