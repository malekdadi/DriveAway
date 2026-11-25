package com.example.maintenance_service.service;

import com.example.maintenance_service.domain.Immobilisation;
import com.example.maintenance_service.domain.StatutImmobilisation;
import com.example.maintenance_service.repository.ImmobilisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImmobilisationService {

    private final ImmobilisationRepository immobilisationRepository;

    public ImmobilisationService(ImmobilisationRepository immobilisationRepository) {
        this.immobilisationRepository = immobilisationRepository;
    }

    public Immobilisation immobiliserVehicule(Long vehiculeId, Long interventionId, String motif, LocalDateTime dateFinPrevue) {
        Optional<Immobilisation> existante = immobilisationRepository.findByVehiculeIdAndStatut(vehiculeId, StatutImmobilisation.ACTIVE);
        if (existante.isPresent()) {
            throw new IllegalStateException("Le véhicule ID " + vehiculeId + " est déjà actuellement immobilisé.");
        }

        Immobilisation immobilisation = new Immobilisation();
        immobilisation.setVehiculeId(vehiculeId);
        immobilisation.setInterventionId(interventionId);
        immobilisation.setDateDebut(LocalDateTime.now());
        immobilisation.setDateFinPrevue(dateFinPrevue);
        immobilisation.setMotif(motif);
        immobilisation.setStatut(StatutImmobilisation.ACTIVE);

        return immobilisationRepository.save(immobilisation);
    }

    public void leverImmobilisation(Long vehiculeId) {
        immobilisationRepository.findByVehiculeIdAndStatut(vehiculeId, StatutImmobilisation.ACTIVE)
                .ifPresent(immob -> {
                    immob.setStatut(StatutImmobilisation.LEVEE);
                    immobilisationRepository.save(immob);
                });
    }

    @Transactional(readOnly = true)
    public boolean isVehiculeImmobilise(Long vehiculeId) {
        return immobilisationRepository.findByVehiculeIdAndStatut(vehiculeId, StatutImmobilisation.ACTIVE).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Immobilisation> getHistoriqueParVehicule(Long vehiculeId) {
        return immobilisationRepository.findByVehiculeId(vehiculeId);
    }
}
