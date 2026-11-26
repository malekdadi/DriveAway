package com.driveaway.reservation.dto;

import com.driveaway.reservation.model.Reservation;
import com.driveaway.reservation.model.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long idReservation,
        Long clientId,
        Long vehiculeId,
        Long agenceRetraitId,
        Long agenceRetourId,
        LocalDateTime dateReservation,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal prixJournalier,
        BigDecimal montantEstime,
        StatutReservation statut
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getIdReservation(), r.getClientId(), r.getVehiculeId(),
                r.getAgenceRetraitId(), r.getAgenceRetourId(), r.getDateReservation(),
                r.getDateDebut(), r.getDateFin(), r.getPrixJournalier(),
                r.getMontantEstime(), r.getStatut());
    }
}
