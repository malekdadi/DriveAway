package com.driveaway.reservation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrat")
@Getter
@Setter
@NoArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Long idContrat;

    // 1 contrat maximum par réservation (UNIQUE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "numero_contrat", nullable = false, unique = true, length = 30)
    private String numeroContrat;

    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @Column(name = "date_debut_effective")
    private LocalDate dateDebutEffective;

    @Column(name = "date_fin_prevue", nullable = false)
    private LocalDate dateFinPrevue;

    @Column(name = "date_retour_effective")
    private LocalDate dateRetourEffective;

    @Column(name = "kilometrage_depart")
    private Integer kilometrageDepart;

    @Column(name = "kilometrage_retour")
    private Integer kilometrageRetour;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "frais_retard", precision = 10, scale = 2)
    private BigDecimal fraisRetard = BigDecimal.ZERO;

    @Column(name = "montant_final", precision = 10, scale = 2)
    private BigDecimal montantFinal;

    @Column(name = "caution", nullable = false, precision = 10, scale = 2)
    private BigDecimal caution;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutContrat statut;
}
