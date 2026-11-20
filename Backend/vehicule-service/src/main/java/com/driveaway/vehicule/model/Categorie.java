package com.driveaway.vehicule.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long idCategorie;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean statut;

    /** Inverse side: one category has many vehicles. */
    @OneToMany(mappedBy = "categorie")
    private List<Vehicule> vehicules = new ArrayList<>();

    public Categorie() {
    }

    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getStatut() { return statut; }
    public void setStatut(Boolean statut) { this.statut = statut; }

    public List<Vehicule> getVehicules() { return vehicules; }
}
