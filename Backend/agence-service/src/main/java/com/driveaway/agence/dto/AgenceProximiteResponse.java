package com.driveaway.agence.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Agence trouvée dans un rayon géographique avec sa distance en kilomètres")
public record AgenceProximiteResponse(

        @Schema(description = "Distance à vol d'oiseau (formule de Haversine) en kilomètres", example = "4.82")
        double distanceKm,

        @Schema(description = "Détails complets de l'agence")
        AgenceResponse agence
) {
}
