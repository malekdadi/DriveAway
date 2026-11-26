package com.driveaway.agence.controller;

import com.driveaway.agence.dto.PointRetraitParametresPatchRequest;
import com.driveaway.agence.dto.PointRetraitRequest;
import com.driveaway.agence.dto.PointRetraitResponse;
import com.driveaway.agence.model.TypePointRetrait;
import com.driveaway.agence.service.PointRetraitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agences")
@Tag(name = "Points de retrait", description = "CRUD et paramétrage des points de retrait et de restitution rattachés aux agences")
public class PointRetraitController {

    private final PointRetraitService pointRetraitService;

    public PointRetraitController(PointRetraitService pointRetraitService) {
        this.pointRetraitService = pointRetraitService;
    }

    @GetMapping("/points")
    @Operation(
            summary = "Lister et filtrer tous les points de retrait du réseau",
            description = "Permet de rechercher les points de retrait sur l'ensemble des agences par ville, type ou autorisations."
    )
    @ApiResponse(responseCode = "200", description = "Liste des points de retrait")
    public List<PointRetraitResponse> findAllPoints(
            @Parameter(description = "Filtre partiel sur la ville de l'agence", example = "Tunis")
            @RequestParam(required = false) String ville,
            @Parameter(description = "Filtre sur le type de point")
            @RequestParam(required = false) TypePointRetrait typePoint,
            @Parameter(description = "Filtre sur l'état actif")
            @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Filtre sur retraitAutorise")
            @RequestParam(required = false) Boolean retraitAutorise,
            @Parameter(description = "Filtre sur retourAutorise")
            @RequestParam(required = false) Boolean retourAutorise,
            @Parameter(description = "Filtre sur autreVilleAutorise")
            @RequestParam(required = false) Boolean autreVilleAutorise) {
        return pointRetraitService.findAll(ville, typePoint, actif, retraitAutorise, retourAutorise, autreVilleAutorise);
    }

    @GetMapping("/{agenceId}/points")
    @Operation(
            summary = "Lister les points de retrait d'une agence",
            description = "Sous-ressource hiérarchique exposant les points de retrait d'une agence avec filtres optionnels."
    )
    @ApiResponse(responseCode = "200", description = "Liste des points de retrait de l'agence")
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public List<PointRetraitResponse> findByAgence(
            @Parameter(description = "Identifiant de l'agence", example = "1")
            @PathVariable Long agenceId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Boolean retraitAutorise,
            @RequestParam(required = false) Boolean retourAutorise,
            @RequestParam(required = false) Boolean autreVilleAutorise,
            @RequestParam(required = false) TypePointRetrait typePoint) {
        return pointRetraitService.findByAgence(agenceId, actif, retraitAutorise, retourAutorise, autreVilleAutorise, typePoint);
    }

    @GetMapping("/{agenceId}/points/{pointId}")
    @Operation(summary = "Consulter un point de retrait précis d'une agence")
    @ApiResponse(responseCode = "200", description = "Point de retrait trouvé")
    @ApiResponse(responseCode = "404", description = "Agence ou point de retrait introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PointRetraitResponse findById(
            @Parameter(description = "Identifiant de l'agence", example = "1")
            @PathVariable Long agenceId,
            @Parameter(description = "Identifiant du point de retrait", example = "1")
            @PathVariable Long pointId) {
        return pointRetraitService.findById(agenceId, pointId);
    }

    @PostMapping("/{agenceId}/points")
    @Operation(
            summary = "Ajouter un point de retrait à une agence",
            description = "Crée un point de retrait rattaché à l'agence et retourne 201 Created + en-tête Location. Valeurs par défaut : typePoint=CENTRE_VILLE, actif=true, capaciteMax=20."
    )
    @ApiResponse(responseCode = "201", description = "Point de retrait créé",
            headers = @Header(name = "Location", description = "URI du nouveau point de retrait"))
    @ApiResponse(responseCode = "400", description = "Données invalides ou combinaison retrait/retour/inter-villes incohérente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Doublon d'adresse dans cette agence ou agence fermée définitivement",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<PointRetraitResponse> create(
            @Parameter(description = "Identifiant de l'agence parente", example = "1")
            @PathVariable Long agenceId,
            @Valid @RequestBody PointRetraitRequest request) {
        PointRetraitResponse created = pointRetraitService.create(agenceId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{pointId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{agenceId}/points/{pointId}")
    @Operation(summary = "Remplacer complètement un point de retrait")
    @ApiResponse(responseCode = "200", description = "Point de retrait mis à jour")
    @ApiResponse(responseCode = "400", description = "Données invalides",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence ou point introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Adresse déjà utilisée dans cette agence ou agence fermée définitivement",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PointRetraitResponse update(
            @PathVariable Long agenceId,
            @PathVariable Long pointId,
            @Valid @RequestBody PointRetraitRequest request) {
        return pointRetraitService.update(agenceId, pointId, request);
    }

    @PatchMapping("/{agenceId}/points/{pointId}/parametres")
    @Operation(
            summary = "Modifier partiellement le paramétrage d'un point de retrait",
            description = "Met à jour uniquement les autorisations (retrait, retour, retour inter-villes), le statut actif ou la capacité maximale."
    )
    @ApiResponse(responseCode = "200", description = "Paramètres mis à jour")
    @ApiResponse(responseCode = "400", description = "Paramètres invalides ou incohérents",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence ou point introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Activation refusée sur une agence fermée définitivement",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PointRetraitResponse updateParametres(
            @PathVariable Long agenceId,
            @PathVariable Long pointId,
            @Valid @RequestBody PointRetraitParametresPatchRequest request) {
        return pointRetraitService.updateParametres(agenceId, pointId, request);
    }

    @DeleteMapping("/{agenceId}/points/{pointId}")
    @Operation(summary = "Supprimer un point de retrait")
    @ApiResponse(responseCode = "204", description = "Point de retrait supprimé")
    @ApiResponse(responseCode = "404", description = "Agence ou point introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(
            @PathVariable Long agenceId,
            @PathVariable Long pointId) {
        pointRetraitService.delete(agenceId, pointId);
        return ResponseEntity.noContent().build();
    }
}
