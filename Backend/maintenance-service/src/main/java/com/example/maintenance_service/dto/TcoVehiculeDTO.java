package com.example.maintenance_service.dto;

public class TcoVehiculeDTO {

    private Long vehiculeId;
    private double totalCoutEntretien;
    private int nombreInterventions;
    private boolean seuilReformeAtteint;
    private String recommandation;
    private Double seuilReforme;

    public TcoVehiculeDTO() {
    }

    // Constructeur exact à 5 arguments utilisé dans MaintenanceService (ligne 213)
    public TcoVehiculeDTO(Long vehiculeId, double totalCoutEntretien, int nombreInterventions,
            boolean seuilReformeAtteint, String recommandation) {
        this.vehiculeId = vehiculeId;
        this.totalCoutEntretien = totalCoutEntretien;
        this.nombreInterventions = nombreInterventions;
        this.seuilReformeAtteint = seuilReformeAtteint;
        this.recommandation = recommandation;
    }

    // Constructeur étendu avec seuilReforme si besoin
    public TcoVehiculeDTO(Long vehiculeId, double totalCoutEntretien, int nombreInterventions,
            Double seuilReforme, boolean seuilReformeAtteint, String recommandation) {
        this.vehiculeId = vehiculeId;
        this.totalCoutEntretien = totalCoutEntretien;
        this.nombreInterventions = nombreInterventions;
        this.seuilReforme = seuilReforme;
        this.seuilReformeAtteint = seuilReformeAtteint;
        this.recommandation = recommandation;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public double getTotalCoutEntretien() {
        return totalCoutEntretien;
    }

    public void setTotalCoutEntretien(double totalCoutEntretien) {
        this.totalCoutEntretien = totalCoutEntretien;
    }

    // Alias pour compatibilité
    public Double getCoutTotalMaintenance() {
        return totalCoutEntretien;
    }

    public void setCoutTotalMaintenance(Double coutTotal) {
        this.totalCoutEntretien = coutTotal != null ? coutTotal : 0.0;
    }

    public int getNombreInterventions() {
        return nombreInterventions;
    }

    public void setNombreInterventions(int nombreInterventions) {
        this.nombreInterventions = nombreInterventions;
    }

    public boolean isSeuilReformeAtteint() {
        return seuilReformeAtteint;
    }

    public void setSeuilReformeAtteint(boolean seuilReformeAtteint) {
        this.seuilReformeAtteint = seuilReformeAtteint;
    }

    public boolean isSeuilReformeDepasse() {
        return seuilReformeAtteint;
    }

    public void setSeuilReformeDepasse(boolean seuilReformeDepasse) {
        this.seuilReformeAtteint = seuilReformeDepasse;
    }

    public String getRecommandation() {
        return recommandation;
    }

    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }

    public Double getSeuilReforme() {
        return seuilReforme;
    }

    public void setSeuilReforme(Double seuilReforme) {
        this.seuilReforme = seuilReforme;
    }
}
