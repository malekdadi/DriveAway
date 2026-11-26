package com.driveaway.reservation.service;

import com.driveaway.reservation.dto.ChiffreAffairesResponse;
import com.driveaway.reservation.dto.ContratRequest;
import com.driveaway.reservation.dto.ContratResponse;
import com.driveaway.reservation.dto.DepartRequest;
import com.driveaway.reservation.dto.RetourRequest;
import com.driveaway.reservation.exception.BadRequestException;
import com.driveaway.reservation.exception.ConflictException;
import com.driveaway.reservation.exception.ResourceNotFoundException;
import com.driveaway.reservation.model.*;
import com.driveaway.reservation.repository.ContratRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class ContratService {

    private final ContratRepository contratRepository;
    private final ReservationService reservationService;

    public ContratService(ContratRepository contratRepository, ReservationService reservationService) {
        this.contratRepository = contratRepository;
        this.reservationService = reservationService;
    }

    // ---------- CREATE ----------
    public ContratResponse creer(ContratRequest req) {
        Reservation r = reservationService.trouver(req.reservationId());
        if (r.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflictException("Le contrat ne peut être établi que pour une réservation CONFIRMEE");
        }
        if (contratRepository.existsByReservationIdReservation(r.getIdReservation())) {
            throw new ConflictException("Un contrat existe déjà pour la réservation " + r.getIdReservation());
        }

        Contrat c = new Contrat();
        c.setReservation(r);
        c.setNumeroContrat("TMP-" + UUID.randomUUID().toString().substring(0, 20));
        c.setDateFinPrevue(r.getDateFin());                  // reprise de date_fin
        c.setMontantTotal(r.getMontantEstime());             // reprise de montant_estime
        c.setCaution(req.caution());
        c.setFraisRetard(BigDecimal.ZERO);
        c.setStatut(StatutContrat.EN_COURS);
        c = contratRepository.save(c);

        c.setNumeroContrat(String.format("CTR-%d-%06d", LocalDate.now().getYear(), c.getIdContrat()));
        return ContratResponse.from(c);
    }

    // ---------- READ ----------
    @Transactional(readOnly = true)
    public ContratResponse consulter(Long id) {
        return ContratResponse.from(trouver(id));
    }

    @Transactional(readOnly = true)
    public ContratResponse consulterParNumero(String numero) {
        return ContratResponse.from(contratRepository.findByNumeroContrat(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable : " + numero)));
    }

    @Transactional(readOnly = true)
    public ContratResponse consulterParReservation(Long reservationId) {
        return ContratResponse.from(contratRepository.findByReservationIdReservation(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun contrat pour la réservation " + reservationId)));
    }

    @Transactional(readOnly = true)
    public Page<ContratResponse> lister(StatutContrat statut, Pageable pageable) {
        Page<Contrat> page = (statut == null)
                ? contratRepository.findAll(pageable)
                : contratRepository.findByStatut(statut, pageable);
        return page.map(ContratResponse::from);
    }

    /**
     * Chiffre d'affaires d'une période : somme des montant_final des contrats TERMINE
     * dont la date de retour effective est comprise entre debut et fin (bornes incluses).
     */
    @Transactional(readOnly = true)
    public ChiffreAffairesResponse chiffreAffaires(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BadRequestException("fin doit être postérieure ou égale à debut");
        }
        BigDecimal total = contratRepository.sommeMontantFinal(StatutContrat.TERMINE, debut, fin);
        long nombre = contratRepository.compterParPeriode(StatutContrat.TERMINE, debut, fin);
        return new ChiffreAffairesResponse(debut, fin, nombre, total);
    }

    // ---------- UPDATE ----------
    /** Signature + départ : date de signature, date réelle de départ, km de départ. */
    public ContratResponse enregistrerDepart(Long id, DepartRequest req) {
        Contrat c = trouver(id);
        exigerEnCours(c);
        if (c.getKilometrageDepart() != null) {
            throw new ConflictException("Le départ est déjà enregistré");
        }
        c.setDateSignature(req.dateSignature() != null ? req.dateSignature() : LocalDate.now());
        c.setDateDebutEffective(req.dateDebutEffective() != null ? req.dateDebutEffective() : LocalDate.now());
        c.setKilometrageDepart(req.kilometrageDepart());
        return ContratResponse.from(c);
    }

    /** Retour : calcule frais_retard et montant_final, clôt le contrat et la réservation. */
    public ContratResponse enregistrerRetour(Long id, RetourRequest req) {
        Contrat c = trouver(id);
        exigerEnCours(c);
        if (c.getKilometrageDepart() == null) {
            throw new ConflictException("Enregistrez d'abord le départ du véhicule");
        }
        if (req.kilometrageRetour() < c.getKilometrageDepart()) {
            throw new BadRequestException("kilometrage_retour doit être supérieur ou égal au kilométrage de départ");
        }
        LocalDate retour = req.dateRetourEffective() != null ? req.dateRetourEffective() : LocalDate.now();
        if (retour.isBefore(c.getDateDebutEffective())) {
            throw new BadRequestException("date_retour_effective ne peut pas précéder le départ");
        }

        c.setDateRetourEffective(retour);
        c.setKilometrageRetour(req.kilometrageRetour());

        // Frais de retard progressifs : 1er jour = 1 × prix journalier, jours suivants = 1,5 × prix journalier
        long joursRetard = Math.max(0, ChronoUnit.DAYS.between(c.getDateFinPrevue(), retour));
        BigDecimal frais = calculerFraisRetard(joursRetard, c.getReservation().getPrixJournalier());
        c.setFraisRetard(frais);
        c.setMontantFinal(c.getMontantTotal().add(frais));

        c.setStatut(StatutContrat.TERMINE);
        c.getReservation().setStatut(StatutReservation.TERMINEE);
        return ContratResponse.from(c);
    }

    public ContratResponse modifierCaution(Long id, BigDecimal caution) {
        Contrat c = trouver(id);
        exigerEnCours(c);
        if (caution == null || caution.signum() < 0) {
            throw new BadRequestException("caution doit être >= 0");
        }
        c.setCaution(caution);
        return ContratResponse.from(c);
    }

    public ContratResponse resilier(Long id) {
        Contrat c = trouver(id);
        exigerEnCours(c);
        c.setStatut(StatutContrat.RESILIE);
        c.getReservation().setStatut(StatutReservation.ANNULEE);
        return ContratResponse.from(c);
    }

    // ---------- DELETE ----------
    public void supprimer(Long id) {
        Contrat c = trouver(id);
        if (c.getStatut() == StatutContrat.EN_COURS && c.getKilometrageDepart() != null) {
            throw new ConflictException("Impossible de supprimer un contrat en cours : résiliez-le d'abord");
        }
        contratRepository.delete(c);
    }

    // ---------- Helpers ----------
    /**
     * Frais de retard progressifs :
     * - 1er jour de retard : 1 × prix journalier
     * - à partir du 2e jour : 1,5 × prix journalier par jour supplémentaire.
     * Ex. 100 DT/jour, 3 jours de retard : 100 + 150 + 150 = 400 DT.
     */
    static BigDecimal calculerFraisRetard(long joursRetard, BigDecimal prixJournalier) {
        if (joursRetard <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal premierJour = prixJournalier;
        BigDecimal joursSuivants = prixJournalier
                .multiply(new BigDecimal("1.5"))
                .multiply(BigDecimal.valueOf(joursRetard - 1));
        return premierJour.add(joursSuivants).setScale(2, RoundingMode.HALF_UP);
    }

    private Contrat trouver(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable : " + id));
    }

    private void exigerEnCours(Contrat c) {
        if (c.getStatut() != StatutContrat.EN_COURS) {
            throw new ConflictException("Le contrat est " + c.getStatut() + " : opération impossible");
        }
    }
}