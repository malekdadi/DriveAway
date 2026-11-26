package com.driveaway.reservation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Long idReservation;

    // Références logiques vers les autres microservices (pas de FK)
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "vehicule_id", nullable = false)
    private Long vehiculeId;

    @Column(name = "agence_retrait_id", nullable = false)
    private Long agenceRetraitId;

    @Column(name = "agence_retour_id", nullable = false)
    private Long agenceRetourId;

    @Column(name = "date_reservation", nullable = false, updatable = false)
    private LocalDateTime dateReservation;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "prix_journalier", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixJournalier;

    @Column(name = "montant_estime", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantEstime;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutReservation statut;

    @PrePersist
    void onCreate() {
        if (dateReservation == null) {
            dateReservation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutReservation.EN_ATTENTE;
        }
    }
}
