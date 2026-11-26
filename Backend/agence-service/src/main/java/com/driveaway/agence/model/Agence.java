package com.driveaway.agence.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "agence",
        uniqueConstraints = @UniqueConstraint(name = "uk_agence_nom_ville", columnNames = {"nom", "ville"})
)
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @Column(name = "telephone", nullable = false, length = 25)
    private String telephone;

    @Column(name = "heure_ouverture", nullable = false)
    private LocalTime heureOuverture;

    @Column(name = "heure_fermeture", nullable = false)
    private LocalTime heureFermeture;

    @Column(name = "ouvert_24h", nullable = false)
    private Boolean ouvert24h = false;

    @Column(name = "jours_ouverture", nullable = false, length = 100)
    private String joursOuverture = "LUN,MAR,MER,JEU,VEN,SAM";

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutAgence statut = StatutAgence.ACTIVE;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    /**
     * Cote inverse : pas de suppression en cascade afin d'appliquer la regle metier 409 Conflict
     * tant que l'agence possede encore des points de retrait rattaches.
     */
    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    private List<PointRetrait> pointsRetrait = new ArrayList<>();

    public Agence() {
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

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalTime getHeureOuverture() {
        return heureOuverture;
    }

    public void setHeureOuverture(LocalTime heureOuverture) {
        this.heureOuverture = heureOuverture;
    }

    public LocalTime getHeureFermeture() {
        return heureFermeture;
    }

    public void setHeureFermeture(LocalTime heureFermeture) {
        this.heureFermeture = heureFermeture;
    }

    public Boolean getOuvert24h() {
        return ouvert24h;
    }

    public void setOuvert24h(Boolean ouvert24h) {
        this.ouvert24h = ouvert24h;
    }

    public String getJoursOuverture() {
        return joursOuverture;
    }

    public void setJoursOuverture(String joursOuverture) {
        this.joursOuverture = joursOuverture;
    }

    public StatutAgence getStatut() {
        return statut;
    }

    public void setStatut(StatutAgence statut) {
        this.statut = statut;
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

    public List<PointRetrait> getPointsRetrait() {
        return pointsRetrait;
    }

    public void setPointsRetrait(List<PointRetrait> pointsRetrait) {
        this.pointsRetrait = pointsRetrait;
    }
}
