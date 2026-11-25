package com.example.maintenance_service.repository;

import com.example.maintenance_service.domain.Intervention;
import com.example.maintenance_service.domain.StatutIntervention;
import com.example.maintenance_service.domain.TypeIntervention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    // Recherche simple
    List<Intervention> findByVehiculeId(Long vehiculeId);
    List<Intervention> findByStatut(StatutIntervention statut);

    // MÉTIER SIMPLE : Recherche multi-critères avec Pagination et Tri
    @Query("SELECT i FROM Intervention i WHERE " +
            "(:vehiculeId IS NULL OR i.vehiculeId = :vehiculeId) AND " +
            "(:statut IS NULL OR i.statut = :statut) AND " +
            "(:type IS NULL OR i.type = :type)")
    Page<Intervention> rechercherMultiCriteres(
            @Param("vehiculeId") Long vehiculeId,
            @Param("statut") StatutIntervention statut,
            @Param("type") TypeIntervention type,
            Pageable pageable
    );
}