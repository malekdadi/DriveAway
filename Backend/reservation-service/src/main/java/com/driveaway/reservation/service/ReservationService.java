package com.driveaway.reservation.service;

import com.driveaway.reservation.dto.ProlongationRequest;
import com.driveaway.reservation.dto.ReservationRequest;
import com.driveaway.reservation.dto.ReservationResponse;
import com.driveaway.reservation.exception.BadRequestException;
import com.driveaway.reservation.exception.ConflictException;
import com.driveaway.reservation.exception.ResourceNotFoundException;
import com.driveaway.reservation.model.Reservation;
import com.driveaway.reservation.model.StatutReservation;
import com.driveaway.reservation.repository.ContratRepository;
import com.driveaway.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    /** Durée au-delà de laquelle une réservation EN_ATTENTE non confirmée expire. */
    static final Duration DELAI_EXPIRATION_EN_ATTENTE = Duration.ofHours(24);

    /** Statuts qui bloquent le véhicule sur la période. */
    private static final Set<StatutReservation> STATUTS_BLOQUANTS =
            EnumSet.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final ReservationRepository reservationRepository;
    private final ContratRepository contratRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ContratRepository contratRepository) {
        this.reservationRepository = reservationRepository;
        this.contratRepository = contratRepository;
    }

    // ---------- CREATE ----------
    public ReservationResponse creer(ReservationRequest req) {
        verifierDates(req.dateDebut(), req.dateFin());
        verifierDisponibilite(req.vehiculeId(), req.dateDebut(), req.dateFin(), null);

        Reservation r = new Reservation();
        r.setClientId(req.clientId());
        r.setVehiculeId(req.vehiculeId());
        r.setAgenceRetraitId(req.agenceRetraitId());
        r.setAgenceRetourId(req.agenceRetourId() != null ? req.agenceRetourId() : req.agenceRetraitId());
        r.setDateDebut(req.dateDebut());
        r.setDateFin(req.dateFin());
        r.setPrixJournalier(req.prixJournalier());
        r.setMontantEstime(calculerMontant(req.dateDebut(), req.dateFin(), req.prixJournalier()));
        r.setStatut(StatutReservation.EN_ATTENTE);
        return ReservationResponse.from(reservationRepository.save(r));
    }

    // ---------- READ ----------
    @Transactional(readOnly = true)
    public ReservationResponse consulter(Long id) {
        return ReservationResponse.from(trouver(id));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> rechercher(Long clientId, Long vehiculeId, StatutReservation statut,
                                                LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        return reservationRepository
                .rechercher(clientId, vehiculeId, statut, dateDebut, dateFin, pageable)
                .map(ReservationResponse::from);
    }

    @Transactional(readOnly = true)
    public boolean estDisponible(Long vehiculeId, LocalDate debut, LocalDate fin) {
        verifierDates(debut, fin);
        return !reservationRepository.existsChevauchement(vehiculeId, debut, fin, STATUTS_BLOQUANTS, null);
    }

    /** Historique complet des réservations d'un client (tous statuts), paginé. */
    @Transactional(readOnly = true)
    public Page<ReservationResponse> historiqueClient(Long clientId, Pageable pageable) {
        return rechercher(clientId, null, null, null, null, pageable);
    }

    /** Réservations en cours d'un client : EN_ATTENTE et CONFIRMEE uniquement, les plus récentes d'abord. */
    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsEnCours(Long clientId) {
        return reservationRepository
                .findByClientIdAndStatutIn(clientId, STATUTS_BLOQUANTS, Sort.by(Sort.Direction.DESC, "dateDebut"))
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ---------- UPDATE ----------
    /** Modification intelligente : revérifie la disponibilité et recalcule le montant. */
    public ReservationResponse modifier(Long id, ReservationRequest req) {
        Reservation r = trouver(id);
        if (r.getStatut() != StatutReservation.EN_ATTENTE && r.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflictException("Une réservation " + r.getStatut() + " ne peut plus être modifiée");
        }
        verifierDates(req.dateDebut(), req.dateFin());
        verifierDisponibilite(req.vehiculeId(), req.dateDebut(), req.dateFin(), id);

        r.setClientId(req.clientId());
        r.setVehiculeId(req.vehiculeId());
        r.setAgenceRetraitId(req.agenceRetraitId());
        r.setAgenceRetourId(req.agenceRetourId() != null ? req.agenceRetourId() : req.agenceRetraitId());
        r.setDateDebut(req.dateDebut());
        r.setDateFin(req.dateFin());
        r.setPrixJournalier(req.prixJournalier());
        r.setMontantEstime(calculerMontant(req.dateDebut(), req.dateFin(), req.prixJournalier()));
        return ReservationResponse.from(reservationRepository.save(r));
    }

    /**
     * Prolongation : repousse date_fin si le véhicule est libre sur les jours ajoutés.
     * Recalcule montant_estime et, si un contrat existe, met à jour date_fin_prevue et montant_total.
     */
    public ReservationResponse prolonger(Long id, ProlongationRequest req) {
        Reservation r = trouver(id);
        if (r.getStatut() != StatutReservation.EN_ATTENTE && r.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflictException("Une réservation " + r.getStatut() + " ne peut pas être prolongée");
        }
        LocalDate nouvelleFin = req.nouvelleDateFin();
        if (!nouvelleFin.isAfter(r.getDateFin())) {
            throw new BadRequestException("nouvelle_date_fin doit être postérieure à la date de fin actuelle ("
                    + r.getDateFin() + ")");
        }
        // Seuls les jours ajoutés doivent être libres (la réservation courante est exclue de la recherche)
        verifierDisponibilite(r.getVehiculeId(), r.getDateFin().plusDays(1), nouvelleFin, id);

        r.setDateFin(nouvelleFin);
        r.setMontantEstime(calculerMontant(r.getDateDebut(), nouvelleFin, r.getPrixJournalier()));

        // Option A : le contrat existant suit la réservation
        contratRepository.findByReservationIdReservation(id).ifPresent(c -> {
            c.setDateFinPrevue(nouvelleFin);
            c.setMontantTotal(r.getMontantEstime());
        });
        return ReservationResponse.from(reservationRepository.save(r));
    }

    public ReservationResponse confirmer(Long id) {
        Reservation r = trouver(id);
        if (r.getStatut() != StatutReservation.EN_ATTENTE) {
            throw new ConflictException("Seule une réservation EN_ATTENTE peut être confirmée");
        }
        r.setStatut(StatutReservation.CONFIRMEE);
        return ReservationResponse.from(r);
    }

    public ReservationResponse annuler(Long id) {
        Reservation r = trouver(id);
        if (r.getStatut() == StatutReservation.ANNULEE || r.getStatut() == StatutReservation.TERMINEE) {
            throw new ConflictException("La réservation est déjà " + r.getStatut());
        }
        r.setFraisAnnulation(calculerFraisAnnulation(r.getDateDebut(), r.getMontantEstime(), LocalDateTime.now()));
        r.setStatut(StatutReservation.ANNULEE);
        return ReservationResponse.from(r);
    }

    /**
     * Expiration automatique : toutes les heures (à hh:00), les réservations EN_ATTENTE
     * non confirmées 24 h après leur création passent à ANNULEE, ce qui libère le véhicule.
     * Pas de pénalité : l'annulation vient du système, pas du client.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void expirerReservationsEnAttente() {
        LocalDateTime limite = LocalDateTime.now().minus(DELAI_EXPIRATION_EN_ATTENTE);
        List<Reservation> expirees = reservationRepository
                .findByStatutAndDateReservationBefore(StatutReservation.EN_ATTENTE, limite);
        for (Reservation r : expirees) {
            r.setStatut(StatutReservation.ANNULEE);
            r.setFraisAnnulation(BigDecimal.ZERO.setScale(2));
        }
        if (!expirees.isEmpty()) {
            log.info("{} réservation(s) EN_ATTENTE expirée(s) et annulée(s) : {}", expirees.size(),
                    expirees.stream().map(Reservation::getIdReservation).toList());
        }
    }

    // ---------- DELETE ----------
    public void supprimer(Long id) {
        Reservation r = trouver(id);
        if (contratRepository.existsByReservationIdReservation(id)) {
            throw new ConflictException("Impossible de supprimer : un contrat est lié à cette réservation");
        }
        if (r.getStatut() == StatutReservation.CONFIRMEE) {
            throw new ConflictException("Annulez d'abord la réservation confirmée");
        }
        reservationRepository.delete(r);
    }

    // ---------- Helpers ----------
    Reservation trouver(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + id));
    }

    private void verifierDates(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BadRequestException("date_fin doit être supérieure ou égale à date_debut");
        }
    }

    private void verifierDisponibilite(Long vehiculeId, LocalDate debut, LocalDate fin, Long excludeId) {
        if (reservationRepository.existsChevauchement(vehiculeId, debut, fin, STATUTS_BLOQUANTS, excludeId)) {
            throw new ConflictException("Le véhicule " + vehiculeId + " est déjà réservé sur cette période");
        }
    }

    /**
     * Montant estimé = (nombre de jours × prix_journalier) − remise longue durée.
     * Les deux bornes comptent (même jour = 1 jour).
     * Remise : moins de 7 jours = 0 % ; à partir de 7 jours = 5 % ; à partir de 14 jours = 10 %.
     */
    static BigDecimal calculerMontant(LocalDate debut, LocalDate fin, BigDecimal prixJournalier) {
        long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
        BigDecimal montantBrut = prixJournalier.multiply(BigDecimal.valueOf(jours));
        BigDecimal remise = montantBrut
                .multiply(BigDecimal.valueOf(tauxRemisePourcent(jours)))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return montantBrut.subtract(remise).setScale(2, RoundingMode.HALF_UP);
    }

    /** Délai minimal avant le début de la location pour une annulation gratuite. */
    static final Duration DELAI_ANNULATION_GRATUITE = Duration.ofHours(48);

    /** Pénalité appliquée en cas d'annulation tardive (en % du montant estimé). */
    static final int TAUX_PENALITE_ANNULATION_POURCENT = 20;

    /**
     * Pénalité d'annulation :
     * plus de 48 h avant le début (date_debut à 00:00) = gratuite ;
     * 48 h ou moins (y compris après le début) = 20 % du montant estimé.
     */
    static BigDecimal calculerFraisAnnulation(LocalDate dateDebut, BigDecimal montantEstime, LocalDateTime maintenant) {
        Duration avantDebut = Duration.between(maintenant, dateDebut.atStartOfDay());
        if (avantDebut.compareTo(DELAI_ANNULATION_GRATUITE) > 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return montantEstime
                .multiply(BigDecimal.valueOf(TAUX_PENALITE_ANNULATION_POURCENT))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Taux de remise (en %) selon la durée de location en jours. */
    static int tauxRemisePourcent(long jours) {
        if (jours >= 14) {
            return 10;
        }
        if (jours >= 7) {
            return 5;
        }
        return 0;
    }
}