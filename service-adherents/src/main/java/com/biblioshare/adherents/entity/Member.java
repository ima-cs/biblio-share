package com.biblioshare.adherents.entity;


import jakarta.persistence.*;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nom;
    public String email;
    public String type;
    public boolean suspendu;
    private boolean suspenduPourAmendes;

    public boolean isSuspendu() {
        return suspendu || suspenduPourAmendes;
    }

    public void suspendrePourAmendes(boolean valeur) {
        suspenduPourAmendes = valeur;
    }
}
