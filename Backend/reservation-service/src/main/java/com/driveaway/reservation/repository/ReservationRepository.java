package com.driveaway.reservation.repository;

import com.driveaway.reservation.model.Reservation;
import com.driveaway.reservation.model.StatutReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Réservations d'un client filtrées sur une liste de statuts (ex. EN_ATTENTE, CONFIRMEE). */
    List<Reservation> findByClientIdAndStatutIn(Long clientId,
                                                Collection<StatutReservation> statuts,
                                                Sort sort);

    /** Réservations actives du véhicule qui chevauchent la période [debut, fin]. */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.vehiculeId = :vehiculeId
              AND r.statut IN :statuts
              AND r.dateDebut <= :fin
              AND r.dateFin >= :debut
              AND (:excludeId IS NULL OR r.idReservation <> :excludeId)
            """)
    boolean existsChevauchement(@Param("vehiculeId") Long vehiculeId,
                                @Param("debut") LocalDate debut,
                                @Param("fin") LocalDate fin,
                                @Param("statuts") Collection<StatutReservation> statuts,
                                @Param("excludeId") Long excludeId);

    /** Recherche multi-critères (tous les paramètres sont optionnels). */
    @Query("""
            SELECT r FROM Reservation r
            WHERE (:clientId IS NULL OR r.clientId = :clientId)
              AND (:vehiculeId IS NULL OR r.vehiculeId = :vehiculeId)
              AND (:statut IS NULL OR r.statut = :statut)
              AND (:dateDebut IS NULL OR r.dateFin >= :dateDebut)
              AND (:dateFin IS NULL OR r.dateDebut <= :dateFin)
            """)
    Page<Reservation> rechercher(@Param("clientId") Long clientId,
                                 @Param("vehiculeId") Long vehiculeId,
                                 @Param("statut") StatutReservation statut,
                                 @Param("dateDebut") LocalDate dateDebut,
                                 @Param("dateFin") LocalDate dateFin,
                                 Pageable pageable);
}