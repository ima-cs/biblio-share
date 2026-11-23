package com.biblioshare.catalogue.dto;


import com.biblioshare.catalogue.entity.Book;
import jakarta.validation.constraints.*;

public record BookRequest(@NotBlank String titre, @NotBlank String auteur, @NotBlank String isbn, @NotBlank String categorie) {
    public Book toEntity() {
        Book book = new Book();
        book.setTitre(titre);
        book.setAuteur(auteur);
        book.setIsbn(isbn);
        book.setCategorie(categorie);
        return book;
    }
}
