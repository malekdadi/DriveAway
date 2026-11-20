package com.driveaway.vehicule.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicule")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    private Long idVehicule;

    @Column(nullable = false, unique = true, length = 20)
    private String immatriculation;

    @Column(nullable = false, length = 50)
    private String marque;

    @Column(nullable = false, length = 50)
    private String modele;

    private Integer annee;

    private Integer kilometrage;

    @Column(nullable = false, length = 20)
    private String statut;

    /** Comma-separated image URLs for this vehicle */
    @Column(length = 2000)
    private String images;

    /** Owning side: many vehicles belong to one category. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    public Vehicule() {
    }

    public Long getIdVehicule() { return idVehicule; }
    public void setIdVehicule(Long idVehicule) { this.idVehicule = idVehicule; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    /** Helper: returns image URLs as a list */
    public java.util.List<String> getImageList() {
        if (images == null || images.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(images.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
