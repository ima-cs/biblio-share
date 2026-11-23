package com.biblioshare.pret.dto;


import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LoanRequest(@NotNull @Positive Long copyId, @NotNull @Positive Long memberId,
                          @NotNull LocalDate dateRetourPrevue) {
}
