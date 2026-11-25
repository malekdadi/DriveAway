package com.example.maintenance_service.controller;

import com.example.maintenance_service.domain.Immobilisation;
import com.example.maintenance_service.service.ImmobilisationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance/immobilisations")
@CrossOrigin(origins = "*")
public class ImmobilisationController {

    private final ImmobilisationService immobilisationService;

    public ImmobilisationController(ImmobilisationService immobilisationService) {
        this.immobilisationService = immobilisationService;
    }

    // Déclarer l'immobilisation d'un véhicule
    @PostMapping
    public ResponseEntity<Immobilisation> immobiliserVehicule(@RequestBody Immobilisation request) {
        Immobilisation immobilisation = immobilisationService.immobiliserVehicule(
                request.getVehiculeId(),
                request.getInterventionId(),
                request.getMotif(),
                request.getDateFinPrevue()
        );
        return new ResponseEntity<>(immobilisation, HttpStatus.CREATED);
    }

    // Lever l'immobilisation d'un véhicule
    @PutMapping("/vehicule/{vehiculeId}/lever")
    public ResponseEntity<Void> leverImmobilisation(@PathVariable Long vehiculeId) {
        immobilisationService.leverImmobilisation(vehiculeId);
        return ResponseEntity.noContent().build();
    }

    // Vérifier si un véhicule est actuellement immobilisé
    @GetMapping("/vehicule/{vehiculeId}/est-immobilise")
    public ResponseEntity<Boolean> isVehiculeImmobilise(@PathVariable Long vehiculeId) {
        return ResponseEntity.ok(immobilisationService.isVehiculeImmobilise(vehiculeId));
    }

    // Consulter l'historique des immobilisations d'un véhicule
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<Immobilisation>> getHistoriqueParVehicule(@PathVariable Long vehiculeId) {
        return ResponseEntity.ok(immobilisationService.getHistoriqueParVehicule(vehiculeId));
    }
}
