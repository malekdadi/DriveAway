package com.driveaway.agence.dto;

import com.driveaway.agence.model.StatutAgence;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "Résultat de vérification d'ouverture d'une agence à une date et heure données")
public record OuvertureResponse(

        @Schema(example = "1")
        Long agenceId,

        @Schema(example = "La Marsa")
        String agenceNom,

        @Schema(example = "ACTIVE")
        StatutAgence statut,

        @Schema(description = "Date et heure évaluées (ISO 8601, fuseau Africa/Tunis)", example = "2026-11-25T14:30:00")
        LocalDateTime dateHeureVerifiee,

        @Schema(description = "True si l'agence est active et ouverte au créneau demandé", example = "true")
        boolean ouverte,

        @Schema(description = "Explication lisible du résultat", example = "L'agence est ouverte au créneau demandé.")
        String raison,

        @Schema(example = "08:00:00", type = "string")
        LocalTime heureOuverture,

        @Schema(example = "20:00:00", type = "string")
        LocalTime heureFermeture,

        @Schema(example = "false")
        Boolean ouvert24h,

        @Schema(example = "LUN,MAR,MER,JEU,VEN,SAM")
        String joursOuverture
) {
}
