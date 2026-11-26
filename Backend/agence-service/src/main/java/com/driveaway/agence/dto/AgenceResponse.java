package com.driveaway.agence.dto;

import com.driveaway.agence.model.StatutAgence;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Représentation JSON d'une agence DriveAway et de ses points de retrait")
public record AgenceResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Tunis-Carthage")
        String nom,

        @Schema(example = "Aéroport International Tunis-Carthage, Zone Arrivées")
        String adresse,

        @Schema(example = "Tunis")
        String ville,

        @Schema(example = "+216 71 755 000")
        String telephone,

        @Schema(example = "00:00:00", type = "string")
        LocalTime heureOuverture,

        @Schema(example = "23:59:59", type = "string")
        LocalTime heureFermeture,

        @Schema(example = "true")
        Boolean ouvert24h,

        @Schema(example = "LUN,MAR,MER,JEU,VEN,SAM,DIM")
        String joursOuverture,

        @Schema(example = "ACTIVE")
        StatutAgence statut,

        @Schema(description = "Indique si l'agence est ouverte à l'instant présent (fuseau Africa/Tunis)", example = "true")
        boolean ouverteMaintenant,

        @Schema(example = "36.8510")
        Double latitude,

        @Schema(example = "10.2272")
        Double longitude,

        @Schema(description = "Nombre total de points de retrait rattachés à l'agence", example = "2")
        int nombrePointsRetrait,

        @Schema(description = "Points de retrait et de restitution rattachés à cette agence")
        List<PointRetraitResponse> pointsRetrait
) {
}
