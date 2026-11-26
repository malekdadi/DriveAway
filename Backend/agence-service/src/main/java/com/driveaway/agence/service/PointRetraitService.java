package com.driveaway.agence.service;

import com.driveaway.agence.dto.PointRetraitParametresPatchRequest;
import com.driveaway.agence.dto.PointRetraitRequest;
import com.driveaway.agence.dto.PointRetraitResponse;
import com.driveaway.agence.exception.BadRequestException;
import com.driveaway.agence.exception.ConflictException;
import com.driveaway.agence.exception.ResourceNotFoundException;
import com.driveaway.agence.mapper.AgenceMapper;
import com.driveaway.agence.model.Agence;
import com.driveaway.agence.model.PointRetrait;
import com.driveaway.agence.model.StatutAgence;
import com.driveaway.agence.model.TypePointRetrait;
import com.driveaway.agence.repository.PointRetraitRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PointRetraitService {

    private final PointRetraitRepository pointRetraitRepository;
    private final AgenceService agenceService;

    public PointRetraitService(PointRetraitRepository pointRetraitRepository, AgenceService agenceService) {
        this.pointRetraitRepository = pointRetraitRepository;
        this.agenceService = agenceService;
    }

    @Transactional(readOnly = true)
    public List<PointRetraitResponse> findByAgence(Long agenceId,
                                                   Boolean actif,
                                                   Boolean retraitAutorise,
                                                   Boolean retourAutorise,
                                                   Boolean autreVilleAutorise,
                                                   TypePointRetrait typePoint) {
        agenceService.getAgenceOrThrow(agenceId);
        Specification<PointRetrait> spec = buildSpecification(
                agenceId, null, typePoint, actif, retraitAutorise, retourAutorise, autreVilleAutorise
        );
        return pointRetraitRepository.findAll(spec, Sort.by("id").ascending()).stream()
                .map(AgenceMapper::toPointResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PointRetraitResponse> findAll(String ville,
                                              TypePointRetrait typePoint,
                                              Boolean actif,
                                              Boolean retraitAutorise,
                                              Boolean retourAutorise,
                                              Boolean autreVilleAutorise) {
        Specification<PointRetrait> spec = buildSpecification(
                null, ville, typePoint, actif, retraitAutorise, retourAutorise, autreVilleAutorise
        );
        return pointRetraitRepository.findAll(spec, Sort.by("id").ascending()).stream()
                .map(AgenceMapper::toPointResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PointRetraitResponse findById(Long agenceId, Long pointId) {
        agenceService.getAgenceOrThrow(agenceId);
        PointRetrait point = getPointOrThrow(agenceId, pointId);
        return AgenceMapper.toPointResponse(point);
    }

    @Transactional
    public PointRetraitResponse create(Long agenceId, PointRetraitRequest request) {
        Agence agence = agenceService.getAgenceOrThrow(agenceId);
        boolean actif = request.actif() != null ? request.actif() : true;

        if (agence.getStatut() == StatutAgence.FERMEE_DEFINITIVEMENT && actif) {
            throw new ConflictException(
                    "Impossible d'ajouter un point de retrait actif à une agence fermée définitivement."
            );
        }

        String adresse = request.adresse().trim();
        if (pointRetraitRepository.existsByAgenceIdAndAdresseIgnoreCase(agenceId, adresse)) {
            throw new ConflictException(
                    "Un point de retrait existe déjà à l'adresse '" + adresse + "' pour l'agence '" + agence.getNom() + "'."
            );
        }

        PointRetrait point = new PointRetrait();
        point.setAgence(agence);
        applyPointRequest(point, request, actif);

        PointRetrait saved = pointRetraitRepository.save(point);
        return AgenceMapper.toPointResponse(saved);
    }

    @Transactional
    public PointRetraitResponse update(Long agenceId, Long pointId, PointRetraitRequest request) {
        Agence agence = agenceService.getAgenceOrThrow(agenceId);
        PointRetrait point = getPointOrThrow(agenceId, pointId);
        boolean actif = request.actif() != null ? request.actif() : true;

        if (agence.getStatut() == StatutAgence.FERMEE_DEFINITIVEMENT && actif) {
            throw new ConflictException(
                    "Impossible d'activer un point de retrait sur une agence fermée définitivement."
            );
        }

        String adresse = request.adresse().trim();
        if (pointRetraitRepository.existsByAgenceIdAndAdresseIgnoreCaseAndIdNot(agenceId, adresse, pointId)) {
            throw new ConflictException(
                    "Un autre point de retrait existe déjà à l'adresse '" + adresse + "' pour cette agence."
            );
        }

        applyPointRequest(point, request, actif);
        PointRetrait saved = pointRetraitRepository.save(point);
        return AgenceMapper.toPointResponse(saved);
    }

    @Transactional
    public PointRetraitResponse updateParametres(Long agenceId, Long pointId, PointRetraitParametresPatchRequest request) {
        if (request.retraitAutorise() == null && request.retourAutorise() == null
                && request.autreVilleAutorise() == null && request.actif() == null
                && request.capaciteMax() == null) {
            throw new BadRequestException("Au moins un paramètre doit être fourni pour la mise à jour partielle.");
        }

        Agence agence = agenceService.getAgenceOrThrow(agenceId);
        PointRetrait point = getPointOrThrow(agenceId, pointId);

        boolean retrait = request.retraitAutorise() != null ? request.retraitAutorise() : Boolean.TRUE.equals(point.getRetraitAutorise());
        boolean retour = request.retourAutorise() != null ? request.retourAutorise() : Boolean.TRUE.equals(point.getRetourAutorise());
        boolean autreVille = request.autreVilleAutorise() != null ? request.autreVilleAutorise() : Boolean.TRUE.equals(point.getAutreVilleAutorise());
        boolean actif = request.actif() != null ? request.actif() : Boolean.TRUE.equals(point.getActif());

        validateRulesCoherence(retrait, retour, autreVille);

        if (agence.getStatut() == StatutAgence.FERMEE_DEFINITIVEMENT && actif) {
            throw new ConflictException(
                    "Impossible d'activer un point de retrait sur une agence fermée définitivement."
            );
        }

        point.setRetraitAutorise(retrait);
        point.setRetourAutorise(retour);
        point.setAutreVilleAutorise(autreVille);
        point.setActif(actif);
        if (request.capaciteMax() != null) {
            point.setCapaciteMax(request.capaciteMax());
        }

        PointRetrait saved = pointRetraitRepository.save(point);
        return AgenceMapper.toPointResponse(saved);
    }

    @Transactional
    public void delete(Long agenceId, Long pointId) {
        agenceService.getAgenceOrThrow(agenceId);
        PointRetrait point = getPointOrThrow(agenceId, pointId);
        pointRetraitRepository.delete(point);
    }

    // ==================== Helpers internes ====================

    private PointRetrait getPointOrThrow(Long agenceId, Long pointId) {
        return pointRetraitRepository.findByIdAndAgenceId(pointId, agenceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Point de retrait introuvable avec l'identifiant " + pointId + " pour l'agence " + agenceId));
    }

    private void applyPointRequest(PointRetrait point, PointRetraitRequest request, boolean actif) {
        validateRulesCoherence(request.retraitAutorise(), request.retourAutorise(), request.autreVilleAutorise());
        if ((request.latitude() == null) != (request.longitude() == null)) {
            throw new BadRequestException("La latitude et la longitude doivent être renseignées ensemble.");
        }

        point.setNom(request.nom().trim());
        point.setAdresse(request.adresse().trim());
        point.setTypePoint(request.typePoint() != null ? request.typePoint() : TypePointRetrait.CENTRE_VILLE);
        point.setRetraitAutorise(request.retraitAutorise());
        point.setRetourAutorise(request.retourAutorise());
        point.setAutreVilleAutorise(request.autreVilleAutorise());
        point.setActif(actif);
        point.setCapaciteMax(request.capaciteMax() != null ? request.capaciteMax() : 20);
        point.setInstructionsAcces(request.instructionsAcces() != null ? request.instructionsAcces().trim() : null);
        point.setLatitude(request.latitude());
        point.setLongitude(request.longitude());
    }

    private void validateRulesCoherence(boolean retraitAutorise, boolean retourAutorise, boolean autreVilleAutorise) {
        if (!retraitAutorise && !retourAutorise) {
            throw new BadRequestException(
                    "Un point de retrait doit autoriser au moins la prise en charge (retrait) ou la restitution (retour)."
            );
        }
        if (autreVilleAutorise && !retourAutorise) {
            throw new BadRequestException(
                    "Le retour inter-villes (autreVilleAutorise) ne peut pas être activé lorsque le retour (retourAutorise) est désactivé."
            );
        }
    }

    private Specification<PointRetrait> buildSpecification(Long agenceId,
                                                           String ville,
                                                           TypePointRetrait typePoint,
                                                           Boolean actif,
                                                           Boolean retraitAutorise,
                                                           Boolean retourAutorise,
                                                           Boolean autreVilleAutorise) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<PointRetrait, Agence> agenceJoin = root.join("agence");

            if (agenceId != null) {
                predicates.add(cb.equal(agenceJoin.get("id"), agenceId));
            }
            if (ville != null && !ville.isBlank()) {
                predicates.add(cb.like(cb.lower(agenceJoin.get("ville")), "%" + ville.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (typePoint != null) {
                predicates.add(cb.equal(root.get("typePoint"), typePoint));
            }
            if (actif != null) {
                predicates.add(cb.equal(root.get("actif"), actif));
            }
            if (retraitAutorise != null) {
                predicates.add(cb.equal(root.get("retraitAutorise"), retraitAutorise));
            }
            if (retourAutorise != null) {
                predicates.add(cb.equal(root.get("retourAutorise"), retourAutorise));
            }
            if (autreVilleAutorise != null) {
                predicates.add(cb.equal(root.get("autreVilleAutorise"), autreVilleAutorise));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
