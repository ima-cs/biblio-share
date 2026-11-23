package com.biblioshare.pret.dto;


public final class RemoteData {
    private RemoteData() {}

    public record Copy(Long id, Long bookId, String codeExemplaire, String etat, boolean disponible) {}
    public record Book(Long id, String titre, String auteur, String isbn, String categorie) {}
    public record Member(Long id, String nom, String email, String type, boolean suspendu) {}
}
