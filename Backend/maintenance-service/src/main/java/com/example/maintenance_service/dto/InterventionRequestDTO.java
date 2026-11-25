package com.example.maintenance_service.dto;

import com.example.maintenance_service.domain.StatutIntervention;
import com.example.maintenance_service.domain.TypeIntervention;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterventionRequestDTO {

    @NotNull(message = "L'ID du véhicule est obligatoire")
    private Long vehiculeId;

    @NotNull(message = "Le type d'intervention est obligatoire")
    private TypeIntervention type;

    private StatutIntervention statut;
    private LocalDateTime dateIntervention;
    private Integer kilometrage;
    private Double coutTotal;
    private String garageNom;

    @Valid
    private List<PieceRechangeDTO> pieces = new ArrayList<>();

    public InterventionRequestDTO() {
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public TypeIntervention getType() {
        return type;
    }

    public void setType(TypeIntervention type) {
        this.type = type;
    }

    public StatutIntervention getStatut() {
        return statut;
    }

    public void setStatut(StatutIntervention statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateIntervention() {
        return dateIntervention;
    }

    public void setDateIntervention(LocalDateTime dateIntervention) {
        this.dateIntervention = dateIntervention;
    }

    public Integer getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(Integer kilometrage) {
        this.kilometrage = kilometrage;
    }

    public Double getCoutTotal() {
        return coutTotal;
    }

    public void setCoutTotal(Double coutTotal) {
        this.coutTotal = coutTotal;
    }

    public String getGarageNom() {
        return garageNom;
    }

    public void setGarageNom(String garageNom) {
        this.garageNom = garageNom;
    }

    public List<PieceRechangeDTO> getPieces() {
        return pieces;
    }

    public void setPieces(List<PieceRechangeDTO> pieces) {
        this.pieces = pieces;
    }
}
