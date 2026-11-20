package com.driveaway.vehicule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category returned by the API")
public record CategorieResponse(

        @Schema(example = "1")
        Long idCategorie,

        @Schema(example = "SUV")
        String nom,

        @Schema(example = "Sport Utility Vehicle")
        String description,

        @Schema(example = "true")
        Boolean statut,

        @Schema(description = "Number of vehicles in this category", example = "3")
        long nombreVehicules
) {
}
