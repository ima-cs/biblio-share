package com.biblioshare.adherents.dto;


import jakarta.validation.constraints.*;

public record ReservationRequest(@NotNull @Positive Long memberId, @NotNull @Positive Long bookId) {}
