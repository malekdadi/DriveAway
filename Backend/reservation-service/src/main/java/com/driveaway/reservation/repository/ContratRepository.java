package com.driveaway.reservation.repository;

import com.driveaway.reservation.model.Contrat;
import com.driveaway.reservation.model.StatutContrat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    Optional<Contrat> findByReservationIdReservation(Long reservationId);

    boolean existsByReservationIdReservation(Long reservationId);

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    Page<Contrat> findByStatut(StatutContrat statut, Pageable pageable);

    /** Contrats dans un statut donné dont la fin prévue est avant une date (réservation chargée, plus ancien retard d'abord). */
    @Query("""
            SELECT c FROM Contrat c JOIN FETCH c.reservation
            WHERE c.statut = :statut
              AND c.dateFinPrevue < :date
            ORDER BY c.dateFinPrevue ASC
            """)
    List<Contrat> findByStatutAndDateFinPrevueAvant(@Param("statut") StatutContrat statut,
                                                    @Param("date") LocalDate date);

    /** Somme des montants finaux des contrats TERMINE dont le retour effectif est dans [debut, fin]. */
    @Query("""
            SELECT COALESCE(SUM(c.montantFinal), 0) FROM Contrat c
            WHERE c.statut = :statut
              AND c.dateRetourEffective BETWEEN :debut AND :fin
            """)
    BigDecimal sommeMontantFinal(@Param("statut") StatutContrat statut,
                                 @Param("debut") LocalDate debut,
                                 @Param("fin") LocalDate fin);

    /** Nombre de contrats pris en compte dans le chiffre d'affaires. */
    @Query("""
            SELECT COUNT(c) FROM Contrat c
            WHERE c.statut = :statut
              AND c.dateRetourEffective BETWEEN :debut AND :fin
            """)
    long compterParPeriode(@Param("statut") StatutContrat statut,
                           @Param("debut") LocalDate debut,
                           @Param("fin") LocalDate fin);
}