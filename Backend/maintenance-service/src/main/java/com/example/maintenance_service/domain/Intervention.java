package com.example.maintenance_service.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interventions")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID du véhicule est obligatoire")
    private Long vehiculeId;

    @NotNull(message = "Le type d'intervention est obligatoire")
    @Enumerated(EnumType.STRING)
    private TypeIntervention type;

    @Enumerated(EnumType.STRING)
    private StatutIntervention statut = StatutIntervention.PLANIFIEE;

    private LocalDateTime dateIntervention;
    private Integer kilometrage;
    private Double coutTotal = 0.0;
    private String garageNom;

    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PieceRechange> pieces = new ArrayList<>();

    public Intervention() {
    }

    // Méthode utilitaire pour lier proprement une pièce
    public void ajouterPiece(PieceRechange piece) {
        pieces.add(piece);
        piece.setIntervention(this);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public TypeIntervention getType() { return type; }
    public void setType(TypeIntervention type) { this.type = type; }

    public StatutIntervention getStatut() { return statut; }
    public void setStatut(StatutIntervention statut) { this.statut = statut; }

    public LocalDateTime getDateIntervention() { return dateIntervention; }
    public void setDateIntervention(LocalDateTime dateIntervention) { this.dateIntervention = dateIntervention; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }

    public Double getCoutTotal() { return coutTotal; }
    public void setCoutTotal(Double coutTotal) { this.coutTotal = coutTotal; }

    public String getGarageNom() { return garageNom; }
    public void setGarageNom(String garageNom) { this.garageNom = garageNom; }

    public List<PieceRechange> getPieces() { return pieces; }
    public void setPieces(List<PieceRechange> pieces) { this.pieces = pieces; }
}