package com.driveaway.agence.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Indicateurs statistiques agrégés du réseau d'agences et de points de retrait")
public record ReseauStatistiquesResponse(

        @Schema(example = "5")
        long totalAgences,

        @Schema(example = "4")
        long agencesActives,

        @Schema(example = "1")
        long agencesFermeesTemporairement,

        @Schema(example = "0")
        long agencesFermeesDefinitivement,

        @Schema(example = "7")
        long totalPointsRetrait,

        @Schema(example = "7")
        long pointsActifs,

        @Schema(example = "7")
        long pointsRetraitAutorise,

        @Schema(example = "6")
        long pointsRetourAutorise,

        @Schema(example = "5")
        long pointsAutreVilleAutorise,

        @Schema(example = "146")
        long capaciteTotaleActive,

        @Schema(description = "Répartition du nombre d'agences par ville")
        Map<String, Long> agencesParVille,

        @Schema(description = "Répartition du nombre de points de retrait par type")
        Map<String, Long> pointsParType
) {
}
