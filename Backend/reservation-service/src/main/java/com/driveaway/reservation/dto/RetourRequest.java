package com.driveaway.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Retour du véhicule. */
public record RetourRequest(
        LocalDate dateRetourEffective,  // défaut : aujourd'hui
        @NotNull(message = "kilometrage_retour obligatoire")
        @Min(value = 0, message = "kilometrage_retour doit être >= 0") Integer kilometrageRetour
) {
}
