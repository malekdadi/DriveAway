package com.driveaway.vehicule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Data used to create or update a vehicle")
public record VehiculeRequest(

        @Schema(description = "License plate", example = "TU-1234-AB")
        @NotBlank(message = "L'immatriculation est obligatoire")
        @Size(max = 20, message = "L'immatriculation ne doit pas dépasser 20 caractères")
        String immatriculation,

        @Schema(description = "Brand", example = "Renault")
        @NotBlank(message = "La marque est obligatoire")
        @Size(max = 50, message = "La marque ne doit pas dépasser 50 caractères")
        String marque,

        @Schema(description = "Model", example = "Clio")
        @NotBlank(message = "Le modèle est obligatoire")
        @Size(max = 50, message = "Le modèle ne doit pas dépasser 50 caractères")
        String modele,

        @Schema(description = "Year of manufacture", example = "2023")
        @Min(value = 1900, message = "L'année doit être >= 1900")
        @Max(value = 2100, message = "L'année doit être <= 2100")
        Integer annee,

        @Schema(description = "Current mileage in km", example = "15000")
        @Min(value = 0, message = "Le kilométrage ne peut pas être négatif")
        Integer kilometrage,

        @Schema(description = "Vehicle status", example = "DISPONIBLE")
        @NotBlank(message = "Le statut est obligatoire")
        String statut,

        @Schema(description = "Category ID", example = "1")
        @NotNull(message = "L'identifiant de la catégorie est obligatoire")
        Long idCategorie
) {
}
