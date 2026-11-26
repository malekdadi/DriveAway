package com.driveaway.agence.dto;

import com.driveaway.agence.model.TypePointRetrait;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Représentation JSON d'un point de retrait / restitution")
public record PointRetraitResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "1")
        Long agenceId,

        @Schema(example = "Tunis-Carthage")
        String agenceNom,

        @Schema(example = "Tunis")
        String agenceVille,

        @Schema(example = "Terminal 1 — Hall Arrivées")
        String nom,

        @Schema(example = "Aéroport Tunis-Carthage, Terminal 1, Guichet n°4")
        String adresse,

        @Schema(example = "AEROPORT")
        TypePointRetrait typePoint,

        @Schema(example = "true")
        Boolean retraitAutorise,

        @Schema(example = "true")
        Boolean retourAutorise,

        @Schema(example = "true")
        Boolean autreVilleAutorise,

        @Schema(example = "true")
        Boolean actif,

        @Schema(example = "40")
        Integer capaciteMax,

        @Schema(example = "À droite après la sortie douane, parking dédié niveau P1.")
        String instructionsAcces,

        @Schema(example = "36.8512")
        Double latitude,

        @Schema(example = "10.2275")
        Double longitude
) {
}
