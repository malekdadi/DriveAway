package com.driveaway.agence.dto;

import com.driveaway.agence.model.StatutAgence;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Changement partiel du statut opérationnel d'une agence")
public record AgenceStatutPatchRequest(

        @Schema(description = "Nouveau statut de l'agence", example = "FERMEE_TEMPORAIREMENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le statut est obligatoire")
        StatutAgence statut
) {
}
