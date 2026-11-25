package com.example.maintenance_service.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "immobilisations")
public class Immobilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vehiculeId; // Référence vers vehicule-service
    private Long interventionId;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFinPrevue;

    @Enumerated(EnumType.STRING)
    private StatutImmobilisation statut = StatutImmobilisation.ACTIVE;

    private String motif;

    public Immobilisation() {
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public Long getInterventionId() { return interventionId; }
    public void setInterventionId(Long interventionId) { this.interventionId = interventionId; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDateTime dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public StatutImmobilisation getStatut() { return statut; }
    public void setStatut(StatutImmobilisation statut) { this.statut = statut; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
