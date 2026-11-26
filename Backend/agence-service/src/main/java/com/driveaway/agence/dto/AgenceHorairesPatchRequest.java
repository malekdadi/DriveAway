package com.driveaway.agence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Modification partielle des horaires d'une agence (seuls les champs fournis sont mis à jour)")
public record AgenceHorairesPatchRequest(

        @Schema(description = "Nouvelle heure d'ouverture (ISO 8601)", example = "07:30:00", type = "string")
        LocalTime heureOuverture,

        @Schema(description = "Nouvelle heure de fermeture (ISO 8601)", example = "21:00:00", type = "string")
        LocalTime heureFermeture,

        @Schema(description = "Activer ou désactiver le fonctionnement 24h/24", example = "false")
        Boolean ouvert24h,

        @Schema(description = "Nouveaux jours d'ouverture parmi LUN,MAR,MER,JEU,VEN,SAM,DIM", example = "LUN,MAR,MER,JEU,VEN,SAM,DIM")
        @Size(max = 100, message = "La liste des jours d'ouverture ne doit pas dépasser 100 caractères")
        String joursOuverture
) {
}
