package com.driveaway.vehicule.dto;

import org.springframework.web.multipart.MultipartFile;

public class CategorieFormDTO {
    private Long idCategorie;
    private String nom;
    private String description;
    private Boolean statut = true;
    private String imageUrl;
    private MultipartFile imageFile;

    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getStatut() { return statut; }
    public void setStatut(Boolean statut) { this.statut = statut; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}
