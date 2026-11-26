package com.driveaway.agence.mapper;

import com.driveaway.agence.dto.AgenceResponse;
import com.driveaway.agence.dto.PointRetraitResponse;
import com.driveaway.agence.model.Agence;
import com.driveaway.agence.model.PointRetrait;

import java.util.List;

/**
 * Conversion entre entites JPA et DTOs REST (en camelCase).
 */
public final class AgenceMapper {

    private AgenceMapper() {
    }

    public static PointRetraitResponse toPointResponse(PointRetrait point) {
        Agence agence = point.getAgence();
        return new PointRetraitResponse(
                point.getId(),
                agence.getId(),
                agence.getNom(),
                agence.getVille(),
                point.getNom(),
                point.getAdresse(),
                point.getTypePoint(),
                point.getRetraitAutorise(),
                point.getRetourAutorise(),
                point.getAutreVilleAutorise(),
                point.getActif(),
                point.getCapaciteMax(),
                point.getInstructionsAcces(),
                point.getLatitude(),
                point.getLongitude()
        );
    }

    public static AgenceResponse toAgenceResponse(Agence agence, List<PointRetrait> points, boolean ouverteMaintenant) {
        List<PointRetraitResponse> pointResponses = points.stream()
                .map(AgenceMapper::toPointResponse)
                .toList();

        return new AgenceResponse(
                agence.getId(),
                agence.getNom(),
                agence.getAdresse(),
                agence.getVille(),
                agence.getTelephone(),
                agence.getHeureOuverture(),
                agence.getHeureFermeture(),
                agence.getOuvert24h(),
                agence.getJoursOuverture(),
                agence.getStatut(),
                ouverteMaintenant,
                agence.getLatitude(),
                agence.getLongitude(),
                pointResponses.size(),
                pointResponses
        );
    }
}
