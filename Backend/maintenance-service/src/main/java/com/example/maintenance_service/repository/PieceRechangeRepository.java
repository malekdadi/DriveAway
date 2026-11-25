package com.example.maintenance_service.repository;

import com.example.maintenance_service.domain.PieceRechange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceRechangeRepository extends JpaRepository<PieceRechange, Long> {

    // Trouver toutes les pièces utilisées pour une intervention donnée
    List<PieceRechange> findByInterventionId(Long interventionId);
}