package com.example.maintenance_service.service;

import com.example.maintenance_service.domain.*;
import com.example.maintenance_service.repository.InterventionRepository;
import com.example.maintenance_service.repository.PieceRechangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final PieceRechangeRepository pieceRepository;
    private final ImmobilisationService immobilisationService;

    public InterventionService(InterventionRepository interventionRepository,
                               PieceRechangeRepository pieceRepository,
                               ImmobilisationService immobilisationService) {
        this.interventionRepository = interventionRepository;
        this.pieceRepository = pieceRepository;
        this.immobilisationService = immobilisationService;
    }

    public Intervention planifierIntervention(Intervention intervention) {
        if (intervention.getDateIntervention() == null) {
            intervention.setDateIntervention(LocalDateTime.now());
        }
        intervention.setStatut(StatutIntervention.PLANIFIEE);
        recalculerCoutTotal(intervention);
        return interventionRepository.save(intervention);
    }

    @Transactional(readOnly = true)
    public List<Intervention> getInterventionsParVehicule(Long vehiculeId) {
        return interventionRepository.findByVehiculeId(vehiculeId);
    }

    @Transactional(readOnly = true)
    public Intervention getInterventionById(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention introuvable avec l'ID : " + id));
    }

    public Intervention ajouterPiece(Long interventionId, PieceRechange piece) {
        Intervention intervention = getInterventionById(interventionId);
        intervention.ajouterPiece(piece);
        recalculerCoutTotal(intervention);
        return interventionRepository.save(intervention);
    }

    public Intervention changerStatut(Long id, StatutIntervention nouveauStatut) {
        Intervention intervention = getInterventionById(id);
        intervention.setStatut(nouveauStatut);

        if (nouveauStatut == StatutIntervention.TERMINEE) {
            immobilisationService.leverImmobilisation(intervention.getVehiculeId());
        }

        return interventionRepository.save(intervention);
    }

    private void recalculerCoutTotal(Intervention intervention) {
        double totalPieces = intervention.getPieces().stream()
                .mapToDouble(p -> (p.getPrixUnitaire() != null ? p.getPrixUnitaire() : 0.0) 
                                * (p.getQuantite() != null ? p.getQuantite() : 1))
                .sum();
        intervention.setCoutTotal(totalPieces);

    }
}
