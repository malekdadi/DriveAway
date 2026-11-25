package com.example.maintenance_service.service;

import com.example.maintenance_service.domain.*;
import com.example.maintenance_service.dto.*;
import com.example.maintenance_service.exception.ResourceNotFoundException;
import com.example.maintenance_service.repository.ImmobilisationRepository;
import com.example.maintenance_service.repository.InterventionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceService {

    private static final double SEUIL_REFORME_PAR_DEFAUT = 3000.0; // Seuil TCO pour alerter sur la réforme

    private final InterventionRepository interventionRepository;
    private final ImmobilisationRepository immobilisationRepository;

    public MaintenanceService(InterventionRepository interventionRepository,
                              ImmobilisationRepository immobilisationRepository) {
        this.interventionRepository = interventionRepository;
        this.immobilisationRepository = immobilisationRepository;
    }

    // =========================================================================
    // 1. CRÉER UNE INTERVENTION À PARTIR DU DTO (POST /interventions)
    // =========================================================================
    public Intervention creerIntervention(InterventionRequestDTO dto) {
        Intervention intervention = new Intervention();
        intervention.setVehiculeId(dto.getVehiculeId());
        intervention.setType(dto.getType());
        intervention.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutIntervention.PLANIFIEE);
        intervention.setDateIntervention(dto.getDateIntervention() != null ? dto.getDateIntervention() : LocalDateTime.now());
        intervention.setKilometrage(dto.getKilometrage());
        intervention.setGarageNom(dto.getGarageNom());

        // Ajout des pièces de rechange éventuelles
        if (dto.getPieces() != null && !dto.getPieces().isEmpty()) {
            for (PieceRechangeDTO pDto : dto.getPieces()) {
                PieceRechange piece = new PieceRechange(
                        pDto.getReference(),
                        pDto.getDesignation(),
                        pDto.getQuantite(),
                        pDto.getPrixUnitaire()
                );
                intervention.ajouterPiece(piece);
            }
        }

        // Calcul du coût total (coût des pièces + coût de base si fourni)
        recalculerCoutTotal(intervention, dto.getCoutTotal());

        Intervention saved = interventionRepository.save(intervention);

        // Si l'intervention est planifiée ou en cours, déclencher l'immobilisation
        if (saved.getStatut() == StatutIntervention.PLANIFIEE || saved.getStatut() == StatutIntervention.EN_COURS) {
            declencherImmobilisation(saved.getVehiculeId(), saved.getId(), "Entretien : " + saved.getType());
        }

        return saved;
    }

    // =========================================================================
    // 2. RÉCUPÉRER UNE INTERVENTION PAR SON ID (GET /interventions/{id})
    // =========================================================================
    @Transactional(readOnly = true)
    public Intervention getInterventionById(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention introuvable avec l'ID : " + id));
    }

    // =========================================================================
    // 3. RÉCUPÉRER TOUTES LES INTERVENTIONS
    // =========================================================================
    @Transactional(readOnly = true)
    public List<Intervention> getAllInterventions() {
        return interventionRepository.findAll();
    }

    // =========================================================================
    // 4. RÉCUPÉRER TOUTES LES INTERVENTIONS D'UN VÉHICULE
    // =========================================================================
    @Transactional(readOnly = true)
    public List<Intervention> getInterventionsByVehicule(Long vehiculeId) {
        return interventionRepository.findByVehiculeId(vehiculeId);
    }

    // =========================================================================
    // 5. MODIFIER UNE INTERVENTION (PUT /interventions/{id})
    // =========================================================================
    public Intervention modifierIntervention(Long id, InterventionRequestDTO dto) {
        Intervention intervention = getInterventionById(id);

        if (dto.getVehiculeId() != null) {
            intervention.setVehiculeId(dto.getVehiculeId());
        }
        if (dto.getType() != null) {
            intervention.setType(dto.getType());
        }
        if (dto.getStatut() != null) {
            intervention.setStatut(dto.getStatut());

            // Si terminée ou annulée, lever l'immobilisation du véhicule
            if (dto.getStatut() == StatutIntervention.TERMINEE || dto.getStatut() == StatutIntervention.ANNULEE) {
                leverImmobilisation(intervention.getVehiculeId());
            }
        }
        if (dto.getDateIntervention() != null) {
            intervention.setDateIntervention(dto.getDateIntervention());
        }
        if (dto.getKilometrage() != null) {
            intervention.setKilometrage(dto.getKilometrage());
        }
        if (dto.getGarageNom() != null) {
            intervention.setGarageNom(dto.getGarageNom());
        }

        // Mise à jour des pièces si fournies
        if (dto.getPieces() != null && !dto.getPieces().isEmpty()) {
            intervention.getPieces().clear();
            for (PieceRechangeDTO pDto : dto.getPieces()) {
                PieceRechange piece = new PieceRechange(
                        pDto.getReference(),
                        pDto.getDesignation(),
                        pDto.getQuantite(),
                        pDto.getPrixUnitaire()
                );
                intervention.ajouterPiece(piece);
            }
        }

        recalculerCoutTotal(intervention, dto.getCoutTotal());

        return interventionRepository.save(intervention);
    }

    // =========================================================================
    // 6. MÉTIERS SIMPLES : RECHERCHE MULTI-CRITÈRES + TRI + PAGINATION
    // =========================================================================
    @Transactional(readOnly = true)
    public Page<Intervention> rechercherInterventions(
            Long vehiculeId,
            StatutIntervention statut,
            TypeIntervention type,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return interventionRepository.rechercherMultiCriteres(vehiculeId, statut, type, pageable);
    }

    // =========================================================================
    // 7. TABLEAU DE BORD STATISTIQUES (DASHBOARD - GET /maintenance/dashboard)
    // =========================================================================
    @Transactional(readOnly = true)
    public MaintenanceDashboardDTO getDashboardStats() {
        List<Intervention> all = interventionRepository.findAll();

        long total = all.size();
        long enCours = all.stream()
                .filter(i -> i.getStatut() == StatutIntervention.EN_COURS || i.getStatut() == StatutIntervention.PLANIFIEE)
                .count();

        long immobActives = immobilisationRepository.findAll().stream()
                .filter(imm -> imm.getStatut() == StatutImmobilisation.ACTIVE)
                .count();

        double coutTotal = all.stream()
                .mapToDouble(i -> i.getCoutTotal() != null ? i.getCoutTotal() : 0.0)
                .sum();

        return new MaintenanceDashboardDTO(total, enCours, immobActives, coutTotal);
    }

    // =========================================================================
    // 8. CALCUL TCO & SEUIL DE RÉFORME POUR UN VÉHICULE
    // =========================================================================
    @Transactional(readOnly = true)
    public TcoVehiculeDTO calculerTcoVehicule(Long vehiculeId) {
        List<Intervention> interventions = interventionRepository.findByVehiculeId(vehiculeId);

        double totalCout = interventions.stream()
                .mapToDouble(i -> i.getCoutTotal() != null ? i.getCoutTotal() : 0.0)
                .sum();

        int nombreInterventions = interventions.size();
        boolean reformeDepassee = totalCout >= SEUIL_REFORME_PAR_DEFAUT;

        String recommandation;
        if (reformeDepassee) {
            recommandation = "ALERTE RÉFORME : Le coût cumulé de maintenance (" + totalCout + " DT) dépasse le seuil critique de "
                    + SEUIL_REFORME_PAR_DEFAUT + " DT. Remplacement du véhicule vivement conseillé.";
        } else {
            recommandation = "ÉTAT NORMAL : Le véhicule est en deçà du seuil de réforme (" + (SEUIL_REFORME_PAR_DEFAUT - totalCout) + " DT restants).";
        }

        return new TcoVehiculeDTO(
                vehiculeId,
                totalCout,
                nombreInterventions,
                reformeDepassee,
                recommandation
        );
    }

    // =========================================================================
    // 9. GESTION DE L'IMMOBILISATION DU VÉHICULE
    // =========================================================================
    public Immobilisation declencherImmobilisation(Long vehiculeId, Long interventionId, String motif) {
        Optional<Immobilisation> active = immobilisationRepository.findByVehiculeIdAndStatut(vehiculeId, StatutImmobilisation.ACTIVE);
        if (active.isPresent()) {
            return active.get();
        }

        Immobilisation immob = new Immobilisation();
        immob.setVehiculeId(vehiculeId);
        immob.setInterventionId(interventionId);
        immob.setDateDebut(LocalDateTime.now());
        immob.setStatut(StatutImmobilisation.ACTIVE);
        immob.setMotif(motif);

        return immobilisationRepository.save(immob);
    }

    public void leverImmobilisation(Long vehiculeId) {
        immobilisationRepository.findByVehiculeIdAndStatut(vehiculeId, StatutImmobilisation.ACTIVE)
                .ifPresent(immob -> {
                    immob.setStatut(StatutImmobilisation.LEVEE);
                    immob.setDateFinPrevue(LocalDateTime.now());
                    immobilisationRepository.save(immob);
                });
    }

    @Transactional(readOnly = true)
    public List<Immobilisation> getImmobilisationsActives() {
        return immobilisationRepository.findByStatut(StatutImmobilisation.ACTIVE);
    }

    // =========================================================================
    // 10. RECALCUL DU COÛT TOTAL
    // =========================================================================
    private void recalculerCoutTotal(Intervention intervention, Double coutOptionnelManuel) {
        double totalPieces = intervention.getPieces().stream()
                .mapToDouble(p -> (p.getPrixUnitaire() != null ? p.getPrixUnitaire() : 0.0)
                        * (p.getQuantite() != null ? p.getQuantite() : 1))
                .sum();

        if (totalPieces > 0) {
            intervention.setCoutTotal(totalPieces);
        } else if (coutOptionnelManuel != null) {
            intervention.setCoutTotal(coutOptionnelManuel);
        }
    }
    // =========================================================================
    // MÉTIER AVANCÉ : DÉTECTION AUTOMATIQUE D'ENTRETIEN AU RETOUR DU VÉHICULE
    // Consomme le retour de réservation et planifie l'entretien si nécessaire
    // =========================================================================
    public Optional<Intervention> detecterEntretienAutomatique(Long vehiculeId, Integer kilometrageRetour) {
        // Règle d'entreprise : Si le véhicule dépasse 10 000 km ou un multiple, vidange auto
        if (kilometrageRetour != null && kilometrageRetour % 10000 < 500) {
            Intervention auto = new Intervention();
            auto.setVehiculeId(vehiculeId);
            auto.setType(TypeIntervention.VIDANGE);
            auto.setStatut(StatutIntervention.PLANIFIEE);
            auto.setDateIntervention(LocalDateTime.now().plusDays(1)); // Programmé pour demain
            auto.setKilometrage(kilometrageRetour);
            auto.setGarageNom("Garage Partenaire Auto");
            auto.setCoutTotal(150.0);

            Intervention cree = interventionRepository.save(auto);
            declencherImmobilisation(vehiculeId, cree.getId(), "Détection automatique : Seuil kilométrique de vidange atteint (" + kilometrageRetour + " km)");
            return Optional.of(cree);
        }
        return Optional.empty();
    }
}