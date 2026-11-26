package com.driveaway.reservation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContratRequest(
        @NotNull(message = "reservation_id obligatoire") Long reservationId,
        @NotNull(message = "caution obligatoire")
        @DecimalMin(value = "0.00", message = "caution doit être >= 0") BigDecimal caution
) {
}
