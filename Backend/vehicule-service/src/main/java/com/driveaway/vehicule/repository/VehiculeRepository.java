package com.driveaway.vehicule.repository;

import com.driveaway.vehicule.model.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    boolean existsByImmatriculationAndIdVehiculeNot(String immatriculation, Long idVehicule);

    boolean existsByCategorieIdCategorie(Long idCategorie);

    List<Vehicule> findByStatut(String statut);

    List<Vehicule> findByCategorieIdCategorie(Long idCategorie);

    List<Vehicule> findByMarqueContainingIgnoreCase(String marque);

    List<Vehicule> findByModeleContainingIgnoreCase(String modele);

    List<Vehicule> findByMarqueContainingIgnoreCaseAndStatut(String marque, String statut);

    long countByStatut(String statut);

    long countByCategorieIdCategorie(Long idCategorie);
}
