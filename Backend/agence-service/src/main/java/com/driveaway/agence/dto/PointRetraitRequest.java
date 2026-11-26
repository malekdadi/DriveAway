package com.driveaway.agence.dto;

import com.driveaway.agence.model.TypePointRetrait;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Données requises pour créer ou remplacer complètement un point de retrait")
public record PointRetraitRequest(

        @Schema(description = "Libellé du point de retrait", example = "Terminal 1 — Hall Arrivées", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le nom du point de retrait est obligatoire")
        @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères")
        String nom,

        @Schema(description = "Adresse précise du point (unique au sein d'une agence)", example = "Aéroport Tunis-Carthage, Terminal 1, Guichet n°4", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'adresse du point de retrait est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        @Schema(description = "Catégorie du lieu (par défaut : CENTRE_VILLE)", example = "AEROPORT", defaultValue = "CENTRE_VILLE")
        TypePointRetrait typePoint,

        @Schema(description = "Autorise la prise en charge (retrait) d'un véhicule", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le champ retraitAutorise est obligatoire")
        Boolean retraitAutorise,

        @Schema(description = "Autorise la restitution (retour) d'un véhicule", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le champ retourAutorise est obligatoire")
        Boolean retourAutorise,

        @Schema(description = "Autorise le retour d'un véhicule provenant d'une autre ville (exige retourAutorise=true)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le champ autreVilleAutorise est obligatoire")
        Boolean autreVilleAutorise,

        @Schema(description = "Indique si le point est actuellement actif (par défaut : true)", example = "true", defaultValue = "true")
        Boolean actif,

        @Schema(description = "Capacité maximale de stationnement en nombre de véhicules (par défaut : 20)", example = "30", defaultValue = "20")
        @Min(value = 1, message = "La capacité maximale doit être supérieure ou égale à 1")
        @Max(value = 1000, message = "La capacité maximale ne peut pas dépasser 1000")
        Integer capaciteMax,

        @Schema(description = "Consignes d'accès pour le client", example = "À droite après la sortie douane, parking dédié P1.")
        @Size(max = 500, message = "Les instructions d'accès ne doivent pas dépasser 500 caractères")
        String instructionsAcces,

        @Schema(description = "Latitude GPS optionnelle (-90 à 90)", example = "36.8512")
        @DecimalMin(value = "-90.0", message = "La latitude doit être comprise entre -90 et 90")
        @DecimalMax(value = "90.0", message = "La latitude doit être comprise entre -90 et 90")
        Double latitude,

        @Schema(description = "Longitude GPS optionnelle (-180 à 180)", example = "10.2275")
        @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180")
        @DecimalMax(value = "180.0", message = "La longitude doit être comprise entre -180 et 180")
        Double longitude
) {
}
