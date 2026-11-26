package com.driveaway.agence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Modification partielle des paramètres opérationnels d'un point de retrait")
public record PointRetraitParametresPatchRequest(

        @Schema(description = "Autoriser ou interdire le retrait", example = "true")
        Boolean retraitAutorise,

        @Schema(description = "Autoriser ou interdire le retour", example = "true")
        Boolean retourAutorise,

        @Schema(description = "Autoriser ou interdire le retour inter-villes (exige retourAutorise=true)", example = "false")
        Boolean autreVilleAutorise,

        @Schema(description = "Activer ou désactiver le point de retrait", example = "true")
        Boolean actif,

        @Schema(description = "Nouvelle capacité maximale en véhicules", example = "25")
        @Min(value = 1, message = "La capacité maximale doit être supérieure ou égale à 1")
        @Max(value = 1000, message = "La capacité maximale ne peut pas dépasser 1000")
        Integer capaciteMax
) {
}
