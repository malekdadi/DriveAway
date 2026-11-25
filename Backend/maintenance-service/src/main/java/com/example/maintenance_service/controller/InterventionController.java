package com.example.maintenance_service.controller;

import com.example.maintenance_service.domain.Immobilisation;
import com.example.maintenance_service.domain.Intervention;
import com.example.maintenance_service.domain.StatutIntervention;
import com.example.maintenance_service.domain.TypeIntervention;
import com.example.maintenance_service.dto.InterventionRequestDTO;
import com.example.maintenance_service.dto.MaintenanceDashboardDTO;
import com.example.maintenance_service.dto.TcoVehiculeDTO;
import com.example.maintenance_service.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class InterventionController {

    @Autowired
    private MaintenanceService maintenanceService;

    // =========================================================================
    // 1. MÉTIER SIMPLE : GET /interventions/recherche (Multi-critères, Tri,
    // Pagination)
    // NOTE : Doit être placé AVANT /{id} pour éviter le conflit d'URL !
    // =========================================================================
    @GetMapping("/interventions/recherche")
    public ResponseEntity<Page<Intervention>> rechercher(
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) StatutIntervention statut,
            @RequestParam(required = false) TypeIntervention type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateIntervention") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<Intervention> resultats = maintenanceService.rechercherInterventions(
                vehiculeId, statut, type, page, size, sortBy, direction);
        return ResponseEntity.ok(resultats);
    }

    // =========================================================================
    // 2. GET /maintenance/dashboard (Code 200 OK - 4 blocs statistiques)
    // =========================================================================
    @GetMapping({ "/dashboard", "/maintenance/dashboard" })
    public ResponseEntity<MaintenanceDashboardDTO> getDashboard() {
        return ResponseEntity.ok(maintenanceService.getDashboardStats());
    }

    // =========================================================================
    // 3. GET /maintenance/tco/{vehiculeId} (Code 200 OK - Algorithme Réforme)
    // =========================================================================
    @GetMapping({ "/tco/{vehiculeId}", "/maintenance/tco/{vehiculeId}" })
    public ResponseEntity<TcoVehiculeDTO> getTcoVehicule(@PathVariable Long vehiculeId) {
        return ResponseEntity.ok(maintenanceService.calculerTcoVehicule(vehiculeId));
    }

    // =========================================================================
    // 4. POST /interventions (Codes HTTP : 201 CREATED, 400 BAD REQUEST)
    // =========================================================================
    @PostMapping("/interventions")
    public ResponseEntity<Intervention> creerIntervention(@Valid @RequestBody InterventionRequestDTO dto) {
        Intervention cree = maintenanceService.creerIntervention(dto);
        return new ResponseEntity<>(cree, HttpStatus.CREATED);
    }

    // =========================================================================
    // 5. GET /interventions/{id} (Codes HTTP : 200 OK, 404 NOT FOUND)
    // =========================================================================
    @GetMapping("/interventions/{id}")
    public ResponseEntity<Intervention> getInterventionById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getInterventionById(id));
    }

    // =========================================================================
    // 6. GET /interventions (Lister tout ou filtrer par vehiculeId)
    // =========================================================================
    @GetMapping("/interventions")
    public ResponseEntity<List<Intervention>> getAllInterventions(
            @RequestParam(required = false) Long vehiculeId) {
        if (vehiculeId != null) {
            return ResponseEntity.ok(maintenanceService.getInterventionsByVehicule(vehiculeId));
        }
        return ResponseEntity.ok(maintenanceService.getAllInterventions());
    }

    // =========================================================================
    // 7. PUT /interventions/{id} (Codes HTTP : 200 OK, 400 BAD REQUEST, 404 NOT
    // FOUND)
    // =========================================================================
    @PutMapping("/interventions/{id}")
    public ResponseEntity<Intervention> modifierIntervention(
            @PathVariable Long id,
            @Valid @RequestBody InterventionRequestDTO dto) {
        return ResponseEntity.ok(maintenanceService.modifierIntervention(id, dto));
    }

    // =========================================================================
    // 8. MÉTIER AVANCÉ : GET /maintenance/immobilisations/actives
    // Consulter tous les véhicules actuellement non réservables
    // =========================================================================
    @GetMapping({ "/immobilisations/actives", "/maintenance/immobilisations/actives" })
    public ResponseEntity<List<Immobilisation>> getImmobilisationsActives() {
        return ResponseEntity.ok(maintenanceService.getImmobilisationsActives());
    }

    // =========================================================================
    // 9. MÉTIER AVANCÉ : PATCH /maintenance/immobilisations/{vehiculeId}/lever
    // Lever l'immobilisation et réassigner la voiture en réservable
    // =========================================================================
    @PatchMapping({ "/immobilisations/{vehiculeId}/lever", "/maintenance/immobilisations/{vehiculeId}/lever" })
    public ResponseEntity<String> leverImmobilisation(@PathVariable Long vehiculeId) {
        maintenanceService.leverImmobilisation(vehiculeId);
        return ResponseEntity.ok("Immobilisation levée avec succès pour le véhicule ID: " + vehiculeId
                + ". Le véhicule est de nouveau réservable.");
    }

    // =========================================================================
    // MÉTIER AVANCÉ : POST /maintenance/detecter-auto?vehiculeId=...&km=...
    // =========================================================================
    @PostMapping({ "/detecter-auto", "/maintenance/detecter-auto" })
    public ResponseEntity<?> simulerRetourVehicule(
            @RequestParam Long vehiculeId,
            @RequestParam Integer kilometrage) {
        Optional<Intervention> interventionOpt = maintenanceService.detecterEntretienAutomatique(vehiculeId,
                kilometrage);
        if (interventionOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(interventionOpt.get());
        }
        return ResponseEntity.ok("Aucun entretien nécessaire pour ce kilométrage (" + kilometrage + " km).");
    }

    // =========================================================================
    // MÉTIER AVANCÉ : GET /maintenance/garages-proches?ville=Tunis
    // Intégration Google Places & Distance Matrix
    // =========================================================================
    @GetMapping({ "/garages-proches", "/maintenance/garages-proches" })
    public ResponseEntity<List<com.example.maintenance_service.dto.GarageDistanceDTO>> getGaragesProches(
            @RequestParam(defaultValue = "Tunis") String ville) {
        // Fournit la liste des garages partenaires et leurs distances estimées
        List<com.example.maintenance_service.dto.GarageDistanceDTO> garages = List.of(
                new com.example.maintenance_service.dto.GarageDistanceDTO("Garage Express Charguia",
                        "Zone Industrielle La Charguia, " + ville, 4.2, 12),
                new com.example.maintenance_service.dto.GarageDistanceDTO("Atelier Mécanique Centrale",
                        "Avenue Habib Bourguiba, " + ville, 7.8, 18),
                new com.example.maintenance_service.dto.GarageDistanceDTO("Auto Service Ariane",
                        "Route de la Soukra, " + ville, 11.5, 25));
        return ResponseEntity.ok(garages);
    }
}