package com.driveaway.reservation.controller;

import com.driveaway.reservation.dto.ChiffreAffairesResponse;
import com.driveaway.reservation.dto.ContratEnRetardResponse;
import com.driveaway.reservation.dto.ContratRequest;
import com.driveaway.reservation.dto.ContratResponse;
import com.driveaway.reservation.dto.DepartRequest;
import com.driveaway.reservation.dto.RetourRequest;
import com.driveaway.reservation.model.StatutContrat;
import com.driveaway.reservation.service.ContratService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Les contrats vivent sous /api/reservations/** pour passer par la route de la Gateway.
 * Le segment littéral "contrats" est prioritaire sur /api/reservations/{id}.
 */
@RestController
@RequestMapping("/api/reservations")
public class ContratController {

    private final ContratService service;

    public ContratController(ContratService service) {
        this.service = service;
    }

    @PostMapping("/contrats")
    public ResponseEntity<ContratResponse> creer(@Valid @RequestBody ContratRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req));
    }

    @GetMapping("/contrats")
    public Page<ContratResponse> lister(@RequestParam(required = false) StatutContrat statut,
                                        @PageableDefault(size = 10, sort = "idContrat") Pageable pageable) {
        return service.lister(statut, pageable);
    }

    /** GET /api/reservations/contrats/chiffre-affaires?debut=2026-12-01&fin=2026-12-31 */
    @GetMapping("/contrats/chiffre-affaires")
    public ChiffreAffairesResponse chiffreAffaires(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return service.chiffreAffaires(debut, fin);
    }

    /** GET /api/reservations/contrats/en-retard : véhicules non rendus à la date prévue. */
    @GetMapping("/contrats/en-retard")
    public List<ContratEnRetardResponse> enRetard() {
        return service.listerEnRetard();
    }

    @GetMapping("/contrats/{id}")
    public ContratResponse consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    /** GET /api/reservations/contrats/{id}/pdf  (200 application/pdf · 404) */
    @GetMapping("/contrats/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        ContratService.ContratPdf pdf = service.genererPdf(id);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename("contrat-" + pdf.numeroContrat() + ".pdf")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.contenu());
    }

    @GetMapping("/contrats/numero/{numero}")
    public ContratResponse consulterParNumero(@PathVariable String numero) {
        return service.consulterParNumero(numero);
    }

    /** GET /reservations/{id}/contrat  (200 · 404) */
    @GetMapping("/{reservationId}/contrat")
    public ContratResponse consulterParReservation(@PathVariable Long reservationId) {
        return service.consulterParReservation(reservationId);
    }

    @PatchMapping("/contrats/{id}/depart")
    public ContratResponse depart(@PathVariable Long id, @Valid @RequestBody DepartRequest req) {
        return service.enregistrerDepart(id, req);
    }

    @PatchMapping("/contrats/{id}/retour")
    public ContratResponse retour(@PathVariable Long id, @Valid @RequestBody RetourRequest req) {
        return service.enregistrerRetour(id, req);
    }

    @PatchMapping("/contrats/{id}/caution")
    public ContratResponse caution(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        return service.modifierCaution(id, body.get("caution"));
    }

    @PatchMapping("/contrats/{id}/resiliation")
    public ContratResponse resilier(@PathVariable Long id) {
        return service.resilier(id);
    }

    @DeleteMapping("/contrats/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}