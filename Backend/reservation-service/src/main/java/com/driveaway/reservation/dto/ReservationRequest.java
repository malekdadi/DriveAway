package com.driveaway.reservation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "client_id obligatoire") Long clientId,
        @NotNull(message = "vehicule_id obligatoire") Long vehiculeId,
        @NotNull(message = "agence_retrait_id obligatoire") Long agenceRetraitId,
        // Si absent : retour dans l'agence de retrait
        Long agenceRetourId,
        @NotNull(message = "date_debut obligatoire") LocalDate dateDebut,
        @NotNull(message = "date_fin obligatoire") LocalDate dateFin,
        @NotNull(message = "prix_journalier obligatoire")
        @DecimalMin(value = "0.01", message = "prix_journalier doit être > 0") BigDecimal prixJournalier
) {
}
