package com.driveaway.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrat EN_COURS dont le véhicule n'a pas été rendu à la date prévue.
 * jours_retard et frais_retard_estimes sont calculés à la date du jour (non stockés).
 */
public record ContratEnRetardResponse(
        Long idContrat,
        String numeroContrat,
        Long reservationId,
        Long clientId,
        Long vehiculeId,
        Long agenceRetourId,
        LocalDate dateDebutEffective,
        LocalDate dateFinPrevue,
        long joursRetard,
        BigDecimal fraisRetardEstimes
) {
}
