package com.driveaway.vehicule.controller;

import com.driveaway.vehicule.dto.CategorieRequest;
import com.driveaway.vehicule.dto.CategorieResponse;
import com.driveaway.vehicule.dto.VehiculeResponse;
import com.driveaway.vehicule.service.CategorieService;
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

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@Tag(name = "Catégories", description = "Gestion des catégories de véhicules")
public class CategorieController {

    private final CategorieService categorieService;
    private final VehiculeService vehiculeService;

    public CategorieController(CategorieService categorieService, VehiculeService vehiculeService) {
        this.categorieService = categorieService;
        this.vehiculeService = vehiculeService;
    }

    @GetMapping
    @Operation(summary = "List all categories")
    @ApiResponse(responseCode = "200", description = "List of categories")
    public List<CategorieResponse> findAll() {
        return categorieService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "List categories with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Paginated categories")
    public Page<CategorieResponse> findPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "idCategorie") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return categorieService.findPage(PageRequest.of(page, size, sort));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by id")
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public CategorieResponse findById(
            @Parameter(description = "Category id", example = "1") @PathVariable Long id) {
        return categorieService.findById(id);
    }

    @GetMapping("/{id}/vehicules")
    @Operation(summary = "List the vehicles of a category")
    @ApiResponse(responseCode = "200", description = "Vehicles of the category")
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public List<VehiculeResponse> findVehicules(
            @Parameter(description = "Category id", example = "1") @PathVariable Long id) {
        return vehiculeService.findByCategorie(id);
    }

    @PostMapping
    @Operation(summary = "Create a category")
    @ApiResponse(responseCode = "201", description = "Category created")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Category name already used",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<CategorieResponse> create(@Valid @RequestBody CategorieRequest request) {
        CategorieResponse created = categorieService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.idCategorie()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    @ApiResponse(responseCode = "200", description = "Category updated")
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Category name already used",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public CategorieResponse update(
            @Parameter(description = "Category id", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategorieRequest request) {
        return categorieService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Refused (409) while the category still has vehicles.")
    @ApiResponse(responseCode = "204", description = "Category deleted")
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Category still has vehicles",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category id", example = "1") @PathVariable Long id) {
        categorieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "List categories by status")
    public List<CategorieResponse> findByStatut(@PathVariable Boolean statut) {
        return categorieService.findByStatut(statut);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories by name")
    public List<CategorieResponse> search(@RequestParam String nom) {
        return categorieService.search(nom);
    }
}
