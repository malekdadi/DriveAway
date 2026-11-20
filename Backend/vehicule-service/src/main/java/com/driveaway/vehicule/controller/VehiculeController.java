package com.driveaway.vehicule.controller;

import com.driveaway.vehicule.dto.VehiculeRequest;
import com.driveaway.vehicule.dto.VehiculeResponse;
import com.driveaway.vehicule.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicules")
@CrossOrigin(origins = "*")
@Tag(name = "Véhicules", description = "Gestion des véhicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    // ==================== CRUD ====================

    @GetMapping
    @Operation(summary = "List all vehicles")
    @ApiResponse(responseCode = "200", description = "List of vehicles")
    public List<VehiculeResponse> findAll() {
        return vehiculeService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "List vehicles with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Paginated vehicles")
    public Page<VehiculeResponse> findPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "idVehicule") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return vehiculeService.findPage(PageRequest.of(page, size, sort));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a vehicle by id")
    @ApiResponse(responseCode = "200", description = "Vehicle found")
    @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public VehiculeResponse findById(
            @Parameter(description = "Vehicle id", example = "1") @PathVariable Long id) {
        return vehiculeService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Add a new vehicle")
    @ApiResponse(responseCode = "201", description = "Vehicle created")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "License plate already used",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<VehiculeResponse> create(@Valid @RequestBody VehiculeRequest request) {
        VehiculeResponse created = vehiculeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.idVehicule()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vehicle")
    @ApiResponse(responseCode = "200", description = "Vehicle updated")
    @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "License plate already used",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public VehiculeResponse update(
            @Parameter(description = "Vehicle id", example = "1") @PathVariable Long id,
            @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle")
    @ApiResponse(responseCode = "204", description = "Vehicle deleted")
    @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(
            @Parameter(description = "Vehicle id", example = "1") @PathVariable Long id) {
        vehiculeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Search / Filter ====================

    @GetMapping("/disponibles")
    @Operation(summary = "List all available vehicles")
    public List<VehiculeResponse> findDisponibles() {
        return vehiculeService.findDisponibles();
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "List vehicles by status")
    public List<VehiculeResponse> findByStatut(
            @Parameter(description = "Status filter", example = "DISPONIBLE") @PathVariable String statut) {
        return vehiculeService.findByStatut(statut);
    }

    @GetMapping("/categorie/{idCategorie}")
    @Operation(summary = "List vehicles of a category")
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public List<VehiculeResponse> findByCategorie(@PathVariable Long idCategorie) {
        return vehiculeService.findByCategorie(idCategorie);
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicles by brand")
    public List<VehiculeResponse> searchByMarque(@RequestParam String marque) {
        return vehiculeService.searchByMarque(marque);
    }

    @GetMapping("/immatriculation/{immatriculation}")
    @Operation(summary = "Find a vehicle by license plate")
    @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public VehiculeResponse findByImmatriculation(@PathVariable String immatriculation) {
        return vehiculeService.findByImmatriculation(immatriculation);
    }

    // ==================== Status & Kilometrage (for inter-service calls) ====================

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Change vehicle status")
    public VehiculeResponse updateStatut(
            @Parameter(description = "Vehicle id", example = "1") @PathVariable Long id,
            @Parameter(description = "New status", example = "LOUE") @RequestParam String statut) {
        return vehiculeService.updateStatut(id, statut);
    }

    @PatchMapping("/{id}/kilometrage")
    @Operation(summary = "Update vehicle mileage")
    public VehiculeResponse updateKilometrage(
            @Parameter(description = "Vehicle id", example = "1") @PathVariable Long id,
            @Parameter(description = "New mileage", example = "25000") @RequestParam Integer kilometrage) {
        return vehiculeService.updateKilometrage(id, kilometrage);
    }

    // ==================== Statistics ====================

    @GetMapping("/stats/count-by-statut")
    @Operation(summary = "Count vehicles grouped by status")
    public Map<String, Long> countByStatut() {
        return vehiculeService.countByStatut();
    }

    @GetMapping("/stats/count-by-categorie/{idCategorie}")
    @Operation(summary = "Count vehicles in a category")
    public Map<String, Long> countByCategorie(@PathVariable Long idCategorie) {
        return Map.of("count", vehiculeService.countByCategorie(idCategorie));
    }
}
