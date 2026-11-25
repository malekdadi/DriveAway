package com.example.maintenance_service.dto;

public class MaintenanceDashboardDTO {

    private long totalInterventions;
    private long interventionsEnCours;
    private long vehiculesImmobilises;
    private double coutTotalGlobal;

    // Champs complémentaires optionnels
    private long interventionsPlanifiees;
    private long interventionsTerminees;

    public MaintenanceDashboardDTO() {
    }

    // Constructeur à 4 arguments utilisé dans MaintenanceService (ligne 188)
    public MaintenanceDashboardDTO(long totalInterventions, long interventionsEnCours, long vehiculesImmobilises,
            double coutTotalGlobal) {
        this.totalInterventions = totalInterventions;
        this.interventionsEnCours = interventionsEnCours;
        this.vehiculesImmobilises = vehiculesImmobilises;
        this.coutTotalGlobal = coutTotalGlobal;
    }

    // Constructeur complet à 6 arguments si besoin
    public MaintenanceDashboardDTO(long totalInterventions, long interventionsPlanifiees,
            long interventionsEnCours, long interventionsTerminees,
            double coutTotalGlobal, long vehiculesImmobilises) {
        this.totalInterventions = totalInterventions;
        this.interventionsPlanifiees = interventionsPlanifiees;
        this.interventionsEnCours = interventionsEnCours;
        this.interventionsTerminees = interventionsTerminees;
        this.coutTotalGlobal = coutTotalGlobal;
        this.vehiculesImmobilises = vehiculesImmobilises;
    }

    public long getTotalInterventions() {
        return totalInterventions;
    }

    public void setTotalInterventions(long totalInterventions) {
        this.totalInterventions = totalInterventions;
    }

    public long getInterventionsEnCours() {
        return interventionsEnCours;
    }

    public void setInterventionsEnCours(long interventionsEnCours) {
        this.interventionsEnCours = interventionsEnCours;
    }

    public long getVehiculesImmobilises() {
        return vehiculesImmobilises;
    }

    public void setVehiculesImmobilises(long vehiculesImmobilises) {
        this.vehiculesImmobilises = vehiculesImmobilises;
    }

    public double getCoutTotalGlobal() {
        return coutTotalGlobal;
    }

    public void setCoutTotalGlobal(double coutTotalGlobal) {
        this.coutTotalGlobal = coutTotalGlobal;
    }

    public long getInterventionsPlanifiees() {
        return interventionsPlanifiees;
    }

    public void setInterventionsPlanifiees(long interventionsPlanifiees) {
        this.interventionsPlanifiees = interventionsPlanifiees;
    }

    public long getInterventionsTerminees() {
        return interventionsTerminees;
    }

    public void setInterventionsTerminees(long interventionsTerminees) {
        this.interventionsTerminees = interventionsTerminees;
    }
}
