package com.driveaway.agence.controller;

import com.driveaway.agence.dto.*;
import com.driveaway.agence.model.StatutAgence;
import com.driveaway.agence.model.TypePointRetrait;
import com.driveaway.agence.service.AgenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agences")
@Tag(name = "Agences", description = "CRUD, recherche, horaires, proximité GPS et statistiques des agences")
public class AgenceController {

    private final AgenceService agenceService;

    public AgenceController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    // ==================== CRUD & Recherche / Tri / Pagination ====================

    @GetMapping
    @Operation(
            summary = "Lister et filtrer les agences",
            description = "Retourne toutes les agences avec filtrage multi-critères et tri en query string (conforme aux bonnes pratiques REST)."
    )
    @ApiResponse(responseCode = "200", description = "Liste des agences correspondant aux filtres")
    @ApiResponse(responseCode = "400", description = "Paramètre de filtre ou de tri invalide",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public List<AgenceResponse> findAll(
            @Parameter(description = "Filtre partiel sur la ville (insensible à la casse)", example = "Tunis")
            @RequestParam(required = false) String ville,
            @Parameter(description = "Filtre partiel sur le nom de l'agence", example = "Marsa")
            @RequestParam(required = false) String nom,
            @Parameter(description = "Filtre sur le statut opérationnel")
            @RequestParam(required = false) StatutAgence statut,
            @Parameter(description = "Filtre sur les agences ouvertes 24h/24")
            @RequestParam(required = false) Boolean ouvert24h,
            @Parameter(description = "Filtre sur les agences ouvertes à l'instant présent (fuseau Africa/Tunis)")
            @RequestParam(required = false) Boolean ouverteMaintenant,
            @Parameter(description = "Filtre sur les agences disposant d'au moins un point actif de ce type")
            @RequestParam(required = false) TypePointRetrait typePoint,
            @Parameter(description = "Filtre sur les agences disposant d'au moins un point actif avec retrait autorisé")
            @RequestParam(required = false) Boolean retraitAutorise,
            @Parameter(description = "Filtre sur les agences disposant d'au moins un point actif avec retour autorisé")
            @RequestParam(required = false) Boolean retourAutorise,
            @Parameter(description = "Filtre sur les agences acceptant le retour inter-villes")
            @RequestParam(required = false) Boolean autreVilleAutorise,
            @Parameter(description = "Champ de tri (id, nom, ville, statut, heureOuverture, heureFermeture, ouvert24h)", example = "nom")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri (asc ou desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return agenceService.findAll(
                ville, nom, statut, ouvert24h, ouverteMaintenant,
                typePoint, retraitAutorise, retourAutorise, autreVilleAutorise,
                sortBy, direction
        );
    }

    @GetMapping("/page")
    @Operation(
            summary = "Lister les agences avec pagination et tri",
            description = "Retourne une page d'agences filtrables et triables."
    )
    @ApiResponse(responseCode = "200", description = "Page d'agences")
    @ApiResponse(responseCode = "400", description = "Paramètre de pagination ou de tri invalide",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public Page<AgenceResponse> findPage(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) StatutAgence statut,
            @RequestParam(required = false) Boolean ouvert24h,
            @RequestParam(required = false) TypePointRetrait typePoint,
            @RequestParam(required = false) Boolean retraitAutorise,
            @RequestParam(required = false) Boolean retourAutorise,
            @RequestParam(required = false) Boolean autreVilleAutorise,
            @Parameter(description = "Numéro de page (>= 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page (1 à 100)", example = "6")
            @RequestParam(defaultValue = "6") int size,
            @Parameter(description = "Champ de tri", example = "nom")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri (asc ou desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return agenceService.findPage(
                ville, nom, statut, ouvert24h, typePoint,
                retraitAutorise, retourAutorise, autreVilleAutorise,
                page, size, sortBy, direction
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une agence par son identifiant")
    @ApiResponse(responseCode = "200", description = "Agence trouvée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide (non numérique)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public AgenceResponse findById(
            @Parameter(description = "Identifiant numérique de l'agence", example = "1")
            @PathVariable Long id) {
        return agenceService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Créer une nouvelle agence",
            description = "Crée une agence et retourne le code 201 Created accompagné de l'en-tête Location. Par défaut, ouvert24h=false, joursOuverture='LUN,MAR,MER,JEU,VEN,SAM' et statut=ACTIVE si omis."
    )
    @ApiResponse(responseCode = "201", description = "Agence créée avec succès",
            headers = @Header(name = "Location", description = "URI de la nouvelle agence créée"))
    @ApiResponse(responseCode = "400", description = "Données d'entrée invalides ou horaires incohérents",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Une agence du même nom existe déjà dans cette ville",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AgenceResponse> create(@Valid @RequestBody AgenceRequest request) {
        AgenceResponse created = agenceService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Remplacer complètement une agence",
            description = "Opération idempotente : remplace l'ensemble des attributs de l'agence."
    )
    @ApiResponse(responseCode = "200", description = "Agence mise à jour")
    @ApiResponse(responseCode = "400", description = "Données invalides",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Nom d'agence déjà utilisé dans cette ville",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public AgenceResponse update(
            @Parameter(description = "Identifiant de l'agence", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AgenceRequest request) {
        return agenceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une agence",
            description = "Opération idempotente : renvoie 204 No Content à la suppression, 404 ensuite, et 409 Conflict tant que l'agence possède des points de retrait rattachés."
    )
    @ApiResponse(responseCode = "204", description = "Agence supprimée")
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "L'agence possède encore des points de retrait",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de l'agence", example = "5")
            @PathVariable Long id) {
        agenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Métiers simples ====================

    @PatchMapping("/{id}/horaires")
    @Operation(
            summary = "Modifier partiellement les horaires d'une agence",
            description = "Met à jour uniquement les horaires, jours d'ouverture ou le mode 24h/24 sans modifier le reste de l'agence."
    )
    @ApiResponse(responseCode = "200", description = "Horaires mis à jour")
    @ApiResponse(responseCode = "400", description = "Horaires ou jours invalides",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public AgenceResponse updateHoraires(
            @Parameter(description = "Identifiant de l'agence", example = "2")
            @PathVariable Long id,
            @Valid @RequestBody AgenceHorairesPatchRequest request) {
        return agenceService.updateHoraires(id, request);
    }

    @PatchMapping("/{id}/statut")
    @Operation(
            summary = "Changer le statut opérationnel d'une agence",
            description = "Permet d'activer, fermer temporairement ou fermer définitivement une agence."
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "400", description = "Statut manquant ou invalide",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public AgenceResponse updateStatut(
            @Parameter(description = "Identifiant de l'agence", example = "2")
            @PathVariable Long id,
            @Valid @RequestBody AgenceStatutPatchRequest request) {
        return agenceService.updateStatut(id, request.statut());
    }

    @GetMapping("/{id}/ouverture")
    @Operation(
            summary = "Vérifier l'ouverture d'une agence à une date et heure données",
            description = "Évalue le statut, le jour et la plage horaire. Si dateHeure est omis, utilise l'heure courante (fuseau Africa/Tunis)."
    )
    @ApiResponse(responseCode = "200", description = "Résultat de vérification d'ouverture")
    @ApiResponse(responseCode = "400", description = "Format de dateHeure invalide (ISO 8601 attendu)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public OuvertureResponse verifierOuverture(
            @Parameter(description = "Identifiant de l'agence", example = "2")
            @PathVariable Long id,
            @Parameter(description = "Date et heure ISO 8601 optionnelle (par défaut : maintenant en Africa/Tunis)", example = "2026-11-25T14:30:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure) {
        return agenceService.verifierOuverture(id, dateHeure);
    }

    @GetMapping("/{id}/retour-autorise")
    @Operation(
            summary = "Vérifier si le retour de véhicule est autorisé dans cette agence",
            description = "Endpoint consommé par reservation-service : vérifie si l'agence est active et dispose de points actifs autorisant le retour (même ville ou inter-villes)."
    )
    @ApiResponse(responseCode = "200", description = "Statut d'autorisation de retour et liste des points compatibles")
    @ApiResponse(responseCode = "404", description = "Agence introuvable",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public RetourAutoriseResponse verifierRetourAutorise(
            @Parameter(description = "Identifiant de l'agence", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Mettre à true pour vérifier spécifiquement le retour inter-villes (par défaut : false)", example = "false")
            @RequestParam(defaultValue = "false") boolean autreVille) {
        return agenceService.verifierRetourAutorise(id, autreVille);
    }

    @GetMapping("/proximite")
    @Operation(
            summary = "Rechercher les agences les plus proches d'une position GPS",
            description = "Calcule la distance réelle en km (formule de Haversine) et trie les agences géolocalisées de la plus proche à la plus éloignée."
    )
    @ApiResponse(responseCode = "200", description = "Agences situées dans le rayon demandé, triées par distance croissante")
    @ApiResponse(responseCode = "400", description = "Coordonnées GPS ou rayon invalides",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public List<AgenceProximiteResponse> findProximite(
            @Parameter(description = "Latitude GPS du client", example = "36.8510", required = true)
            @RequestParam double latitude,
            @Parameter(description = "Longitude GPS du client", example = "10.2272", required = true)
            @RequestParam double longitude,
            @Parameter(description = "Rayon de recherche en km (défaut : 25 km, max : 1000 km)", example = "25")
            @RequestParam(defaultValue = "25") double rayonKm,
            @Parameter(description = "Ne retourner que les agences actives (défaut : true)", example = "true")
            @RequestParam(defaultValue = "true") boolean activeUniquement) {
        return agenceService.findProximite(latitude, longitude, rayonKm, activeUniquement);
    }

    @GetMapping("/statistiques")
    @Operation(
            summary = "Consulter les statistiques agrégées du réseau d'agences",
            description = "Expose les compteurs clés du domaine (agences par statut et ville, points par type et autorisations, capacité active)."
    )
    @ApiResponse(responseCode = "200", description = "Statistiques du réseau")
    public ReseauStatistiquesResponse getStatistiques() {
        return agenceService.getStatistiques();
    }
}
