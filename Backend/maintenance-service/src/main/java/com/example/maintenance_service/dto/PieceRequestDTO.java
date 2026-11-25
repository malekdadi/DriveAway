package com.example.maintenance_service.dto;

public class PieceRequestDTO {
    private String reference;
    private String designation;
    private Integer quantite;
    private Double prixUnitaire;

    public PieceRequestDTO() {
    }

    public PieceRequestDTO(String reference, String designation, Integer quantite, Double prixUnitaire) {
        this.reference = reference;
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}
