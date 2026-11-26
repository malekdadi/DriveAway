package com.driveaway.agence.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Vérification de l'autorisation de retour de véhicule pour une agence (contrat OpenFeign)")
public record RetourAutoriseResponse(

        @Schema(example = "1")
        Long agenceId,

        @Schema(example = "Tunis-Carthage")
        String agenceNom,

        @Schema(example = "Tunis")
        String ville,

        @Schema(description = "Indique si l'agence possède le statut ACTIVE", example = "true")
        boolean agenceActive,

        @Schema(description = "Indique si le retour est autorisé (même ville ou inter-villes selon la demande)", example = "true")
        boolean retourAutorise,

        @Schema(description = "Indique si l'agence accepte les retours provenant d'une autre ville", example = "true")
        boolean autreVilleAutorise,

        @Schema(description = "Nombre de points actifs acceptant ce type de retour", example = "2")
        int nombrePointsCompatibles,

        @Schema(description = "Explication métier du résultat", example = "Retour autorisé sur 2 point(s) actif(s).")
        String raison,

        @Schema(description = "Liste des points de retrait compatibles avec le retour demandé")
        List<PointRetraitResponse> pointsCompatibles
) {
}
