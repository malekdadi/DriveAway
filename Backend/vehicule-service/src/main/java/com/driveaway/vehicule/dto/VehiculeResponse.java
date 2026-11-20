package com.driveaway.vehicule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vehicle returned by the API")
public record VehiculeResponse(

        @Schema(example = "1")
        Long idVehicule,

        @Schema(example = "TU-1234-AB")
        String immatriculation,

        @Schema(example = "Renault")
        String marque,

        @Schema(example = "Clio")
        String modele,

        @Schema(example = "2023")
        Integer annee,

        @Schema(example = "15000")
        Integer kilometrage,

        @Schema(example = "DISPONIBLE")
        String statut,

        @Schema(description = "Category info")
        CategorieResponse categorie
) {
}
