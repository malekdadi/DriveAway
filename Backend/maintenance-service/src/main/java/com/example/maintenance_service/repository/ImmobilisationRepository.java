package com.example.maintenance_service.repository;

import com.example.maintenance_service.domain.Immobilisation;
import com.example.maintenance_service.domain.StatutImmobilisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImmobilisationRepository extends JpaRepository<Immobilisation, Long> {

    // Savoir si un véhicule est actuellement immobilisé (ACTIVE)
    Optional<Immobilisation> findByVehiculeIdAndStatut(Long vehiculeId, StatutImmobilisation statut);

    // Lister toutes les immobilisations d'un véhicule
    List<Immobilisation> findByVehiculeId(Long vehiculeId);

    // Lister les immobilisations par statut (ex: ACTIVE)
    List<Immobilisation> findByStatut(StatutImmobilisation statut);
}