package com.driveaway.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Signature du contrat + départ effectif du véhicule. */
public record DepartRequest(
        LocalDate dateSignature,        // défaut : aujourd'hui
        LocalDate dateDebutEffective,   // défaut : aujourd'hui
        @NotNull(message = "kilometrage_depart obligatoire")
        @Min(value = 0, message = "kilometrage_depart doit être >= 0") Integer kilometrageDepart
) {
}
