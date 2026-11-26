package com.driveaway.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Chiffre d'affaires de l'agence sur une période (contrats TERMINE, selon la date de retour effective). */
public record ChiffreAffairesResponse(
        LocalDate debut,
        LocalDate fin,
        long nombreContrats,
        BigDecimal chiffreAffaires
) {
}