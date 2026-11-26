package com.driveaway.reservation.controller;

import com.driveaway.reservation.dto.ProlongationRequest;
import com.driveaway.reservation.dto.ReservationRequest;
import com.driveaway.reservation.dto.ReservationResponse;
import com.driveaway.reservation.model.StatutReservation;
import com.driveaway.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> creer(@Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req));
    }

    @GetMapping("/{id}")
    public ReservationResponse consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    /**
     * Historique des réservations d'un client : GET /api/reservations?clientId=1
     * (peut aussi être filtré par statut, dates, véhicule ; résultat paginé).
     */
    @GetMapping
    public Page<ReservationResponse> rechercher(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) StatutReservation statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @PageableDefault(size = 10, sort = "dateReservation") Pageable pageable) {
        return service.rechercher(clientId, vehiculeId, statut, dateDebut, dateFin, pageable);
    }

    /** Réservations en cours d'un client (EN_ATTENTE et CONFIRMEE). */
    @GetMapping("/client/{clientId}/en-cours")
    public List<ReservationResponse> enCours(@PathVariable Long clientId) {
        return service.reservationsEnCours(clientId);
    }

    @GetMapping("/disponibilite")
    public Map<String, Object> disponibilite(
            @RequestParam Long vehiculeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return Map.of("vehiculeId", vehiculeId, "disponible", service.estDisponible(vehiculeId, dateDebut, dateFin));
    }

    @PutMapping("/{id}")
    public ReservationResponse modifier(@PathVariable Long id, @Valid @RequestBody ReservationRequest req) {
        return service.modifier(id, req);
    }

    @PatchMapping("/{id}/prolongation")
    public ReservationResponse prolonger(@PathVariable Long id, @Valid @RequestBody ProlongationRequest req) {
        return service.prolonger(id, req);
    }

    @PatchMapping("/{id}/confirmation")
    public ReservationResponse confirmer(@PathVariable Long id) {
        return service.confirmer(id);
    }

    @PatchMapping("/{id}/annulation")
    public ReservationResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}