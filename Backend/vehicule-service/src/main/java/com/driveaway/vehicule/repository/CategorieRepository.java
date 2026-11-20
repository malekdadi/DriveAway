package com.driveaway.vehicule.repository;

import com.driveaway.vehicule.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdCategorieNot(String nom, Long idCategorie);

    List<Categorie> findByStatut(Boolean statut);

    List<Categorie> findByNomContainingIgnoreCase(String nom);
}
