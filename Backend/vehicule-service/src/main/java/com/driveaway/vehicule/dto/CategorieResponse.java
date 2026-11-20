package com.driveaway.vehicule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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

        @Schema(example = "https://example.com/suv.jpg")
        String imageUrl,

        @Schema(description = "Number of vehicles in this category", example = "3")
        long nombreVehicules
) {
}
