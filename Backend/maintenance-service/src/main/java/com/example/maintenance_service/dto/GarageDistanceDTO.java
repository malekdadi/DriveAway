package com.example.maintenance_service.dto;

public class GarageDistanceDTO {
    private String nomGarage;
    private String adresse;
    private double distanceKm;
    private int dureeMinutes;

    public GarageDistanceDTO(String nomGarage, String adresse, double distanceKm, int dureeMinutes) {
        this.nomGarage = nomGarage;
        this.adresse = adresse;
        this.distanceKm = distanceKm;
        this.dureeMinutes = dureeMinutes;
    }

    public String getNomGarage() { return nomGarage; }
    public String getAdresse() { return adresse; }
    public double getDistanceKm() { return distanceKm; }
    public int getDureeMinutes() { return dureeMinutes; }
}