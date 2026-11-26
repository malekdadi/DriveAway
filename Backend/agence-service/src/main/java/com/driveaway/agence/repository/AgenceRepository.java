package com.driveaway.agence.repository;

import com.driveaway.agence.model.Agence;
import com.driveaway.agence.model.StatutAgence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgenceRepository extends JpaRepository<Agence, Long>, JpaSpecificationExecutor<Agence> {

    boolean existsByNomIgnoreCaseAndVilleIgnoreCase(String nom, String ville);

    boolean existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdNot(String nom, String ville, Long id);

    long countByStatut(StatutAgence statut);

    List<Agence> findByLatitudeIsNotNullAndLongitudeIsNotNull();

    @Query("SELECT a.ville, COUNT(a) FROM Agence a GROUP BY a.ville ORDER BY a.ville ASC")
    List<Object[]> countGroupByVille();
}
