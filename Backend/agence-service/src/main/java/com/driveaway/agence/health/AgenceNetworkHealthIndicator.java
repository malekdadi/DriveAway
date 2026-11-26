package com.driveaway.agence.health;

import com.driveaway.agence.model.StatutAgence;
import com.driveaway.agence.repository.AgenceRepository;
import com.driveaway.agence.repository.PointRetraitRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * HealthIndicator personnalise (Chapitre 4 - Observabilite Actuator & lien Eureka).
 * Remonte l'etat operationnel reel du reseau d'agences sur /actuator/health.
 */
@Component("agenceNetwork")
public class AgenceNetworkHealthIndicator implements HealthIndicator {

    private final AgenceRepository agenceRepository;
    private final PointRetraitRepository pointRetraitRepository;

    public AgenceNetworkHealthIndicator(AgenceRepository agenceRepository,
                                        PointRetraitRepository pointRetraitRepository) {
        this.agenceRepository = agenceRepository;
        this.pointRetraitRepository = pointRetraitRepository;
    }

    @Override
    public Health health() {
        try {
            long totalAgences = agenceRepository.count();
            long agencesActives = agenceRepository.countByStatut(StatutAgence.ACTIVE);
            long pointsActifs = pointRetraitRepository.countByActifTrue();

            Health.Builder builder = agencesActives > 0
                    ? Health.up()
                    : Health.down().withDetail("reason", "Aucune agence active disponible dans le réseau");

            return builder
                    .withDetail("totalAgences", totalAgences)
                    .withDetail("agencesActives", agencesActives)
                    .withDetail("pointsRetraitActifs", pointsActifs)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("reason", "Impossible d'interroger la base des agences")
                    .build();
        }
    }
}
