package com.driveaway.vehicule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data used to create or update a category")
public record CategorieRequest(

        @Schema(description = "Category name", example = "SUV")
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Schema(description = "Category description", example = "Sport Utility Vehicle")
        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        @Schema(description = "Active status", example = "true")
        @NotNull(message = "Le statut est obligatoire")
        Boolean statut
) {
}
