package com.driveaway.vehicule.mapper;

import com.driveaway.vehicule.dto.*;
import com.driveaway.vehicule.model.Categorie;
import com.driveaway.vehicule.model.Vehicule;

/** Converts between entities and DTOs. */
public final class VehiculeMapper {

    private VehiculeMapper() {
    }

    // ==================== Categorie ====================

    public static Categorie toEntity(CategorieRequest request) {
        Categorie c = new Categorie();
        c.setNom(request.nom().trim());
        c.setDescription(request.description());
        c.setStatut(request.statut());
        return c;
    }

    public static void updateEntity(Categorie categorie, CategorieRequest request) {
        categorie.setNom(request.nom().trim());
        categorie.setDescription(request.description());
        categorie.setStatut(request.statut());
    }

    public static CategorieResponse toResponse(Categorie categorie) {
        return new CategorieResponse(
                categorie.getIdCategorie(),
                categorie.getNom(),
                categorie.getDescription(),
                categorie.getStatut(),
                categorie.getImageUrl(),
                categorie.getVehicules() != null ? categorie.getVehicules().size() : 0
        );
    }

    // ==================== Vehicule ====================

    public static void updateEntity(Vehicule vehicule, VehiculeRequest request, Categorie categorie) {
        vehicule.setImmatriculation(request.immatriculation().trim());
        vehicule.setMarque(request.marque().trim());
        vehicule.setModele(request.modele().trim());
        vehicule.setAnnee(request.annee());
        vehicule.setKilometrage(request.kilometrage());
        vehicule.setStatut(request.statut());
        vehicule.setCategorie(categorie);
    }

    public static VehiculeResponse toResponse(Vehicule vehicule) {
        return new VehiculeResponse(
                vehicule.getIdVehicule(),
                vehicule.getImmatriculation(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getAnnee(),
                vehicule.getKilometrage(),
                vehicule.getStatut(),
                vehicule.getImageList(),
                toResponse(vehicule.getCategorie())
        );
    }
}
