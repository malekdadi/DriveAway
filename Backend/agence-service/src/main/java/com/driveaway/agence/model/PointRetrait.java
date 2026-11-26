package com.driveaway.agence.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "point_retrait",
        uniqueConstraints = @UniqueConstraint(name = "uk_point_agence_adresse", columnNames = {"agence_id", "adresse"})
)
public class PointRetrait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_point", nullable = false, length = 30)
    private TypePointRetrait typePoint = TypePointRetrait.CENTRE_VILLE;

    @Column(name = "retrait_autorise", nullable = false)
    private Boolean retraitAutorise = true;

    @Column(name = "retour_autorise", nullable = false)
    private Boolean retourAutorise = true;

    @Column(name = "autre_ville_autorise", nullable = false)
    private Boolean autreVilleAutorise = false;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Column(name = "capacite_max", nullable = false)
    private Integer capaciteMax = 20;

    @Column(name = "instructions_acces", length = 500)
    private String instructionsAcces;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    public PointRetrait() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public TypePointRetrait getTypePoint() {
        return typePoint;
    }

    public void setTypePoint(TypePointRetrait typePoint) {
        this.typePoint = typePoint;
    }

    public Boolean getRetraitAutorise() {
        return retraitAutorise;
    }

    public void setRetraitAutorise(Boolean retraitAutorise) {
        this.retraitAutorise = retraitAutorise;
    }

    public Boolean getRetourAutorise() {
        return retourAutorise;
    }

    public void setRetourAutorise(Boolean retourAutorise) {
        this.retourAutorise = retourAutorise;
    }

    public Boolean getAutreVilleAutorise() {
        return autreVilleAutorise;
    }

    public void setAutreVilleAutorise(Boolean autreVilleAutorise) {
        this.autreVilleAutorise = autreVilleAutorise;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public String getInstructionsAcces() {
        return instructionsAcces;
    }

    public void setInstructionsAcces(String instructionsAcces) {
        this.instructionsAcces = instructionsAcces;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }
}
