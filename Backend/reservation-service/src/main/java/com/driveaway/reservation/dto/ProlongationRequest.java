package com.driveaway.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Prolongation d'une réservation : seule la nouvelle date de fin est demandée. */
public record ProlongationRequest(
        @NotNull(message = "nouvelle_date_fin obligatoire") LocalDate nouvelleDateFin
) {
}