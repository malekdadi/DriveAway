package com.driveaway.agence.dto;

import com.driveaway.agence.model.StatutAgence;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "Données requises pour créer ou remplacer complètement une agence")
public record AgenceRequest(

        @Schema(description = "Nom de l'agence (unique dans une même ville)", example = "Tunis-Carthage", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le nom de l'agence est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Schema(description = "Adresse postale complète", example = "Aéroport International Tunis-Carthage, Zone Arrivées", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        @Schema(description = "Ville de l'agence", example = "Tunis", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Schema(description = "Numéro de téléphone joignable", example = "+216 71 755 000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{8,25}$", message = "Le format du numéro de téléphone est invalide")
        String telephone,

        @Schema(description = "Heure d'ouverture au format ISO 8601 (HH:mm:ss)", example = "08:00:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "L'heure d'ouverture est obligatoire")
        LocalTime heureOuverture,

        @Schema(description = "Heure de fermeture au format ISO 8601 (doit être strictement après heureOuverture si ouvert24h=false)", example = "20:00:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "L'heure de fermeture est obligatoire")
        LocalTime heureFermeture,

        @Schema(description = "Indique si l'agence est ouverte 24h/24 (par défaut : false)", example = "false", defaultValue = "false")
        Boolean ouvert24h,

        @Schema(description = "Jours d'ouverture séparés par des virgules parmi LUN,MAR,MER,JEU,VEN,SAM,DIM (par défaut : LUN,MAR,MER,JEU,VEN,SAM)", example = "LUN,MAR,MER,JEU,VEN,SAM", defaultValue = "LUN,MAR,MER,JEU,VEN,SAM")
        @Size(max = 100, message = "La liste des jours d'ouverture ne doit pas dépasser 100 caractères")
        String joursOuverture,

        @Schema(description = "Statut opérationnel de l'agence (par défaut : ACTIVE)", example = "ACTIVE", defaultValue = "ACTIVE")
        StatutAgence statut,

        @Schema(description = "Latitude GPS en degrés décimaux (optionnelle, entre -90 et 90)", example = "36.8510")
        @DecimalMin(value = "-90.0", message = "La latitude doit être comprise entre -90 et 90")
        @DecimalMax(value = "90.0", message = "La latitude doit être comprise entre -90 et 90")
        Double latitude,

        @Schema(description = "Longitude GPS en degrés décimaux (optionnelle, entre -180 et 180)", example = "10.2272")
        @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180")
        @DecimalMax(value = "180.0", message = "La longitude doit être comprise entre -180 et 180")
        Double longitude
) {
}
