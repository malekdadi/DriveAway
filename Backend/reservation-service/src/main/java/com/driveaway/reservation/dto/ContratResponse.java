package com.driveaway.reservation.dto;

import com.driveaway.reservation.model.Contrat;
import com.driveaway.reservation.model.StatutContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratResponse(
        Long idContrat,
        Long reservationId,
        String numeroContrat,
        LocalDate dateSignature,
        LocalDate dateDebutEffective,
        LocalDate dateFinPrevue,
        LocalDate dateRetourEffective,
        Integer kilometrageDepart,
        Integer kilometrageRetour,
        Integer kilometrageParcouru,   // champ calculé (non stocké en base)
        BigDecimal montantTotal,
        BigDecimal fraisRetard,
        BigDecimal montantFinal,
        BigDecimal caution,
        StatutContrat statut
) {
    public static ContratResponse from(Contrat c) {
        return new ContratResponse(
                c.getIdContrat(), c.getReservation().getIdReservation(), c.getNumeroContrat(),
                c.getDateSignature(), c.getDateDebutEffective(), c.getDateFinPrevue(),
                c.getDateRetourEffective(), c.getKilometrageDepart(), c.getKilometrageRetour(),
                calculerKilometrageParcouru(c.getKilometrageDepart(), c.getKilometrageRetour()),
                c.getMontantTotal(), c.getFraisRetard(), c.getMontantFinal(),
                c.getCaution(), c.getStatut());
    }

    /**
     * Kilométrage parcouru = kilométrage retour − kilométrage départ.
     * Renvoie null tant que le départ ou le retour n'est pas encore enregistré.
     */
    static Integer calculerKilometrageParcouru(Integer depart, Integer retour) {
        if (depart == null || retour == null) {
            return null;
        }
        return retour - depart;
    }
}