package com.biblioshare.catalogue.dto;


import com.biblioshare.catalogue.entity.Copy;
import jakarta.validation.constraints.*;

public record CopyRequest(@NotNull @Positive Long bookId, @NotBlank String codeExemplaire, @NotBlank String etat, Boolean disponible) {
    public Copy toEntity() {
        return new Copy(bookId, codeExemplaire, etat, disponible == null || disponible);
    }
}
