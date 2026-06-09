package com.pfe.itsm.interventions.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateInterventionRequest(
        @NotNull UUID ticketId,
        @NotNull UUID technicienId,
        @NotNull @Future Instant dateDebutPrevue,
        @NotNull @Future Instant dateFinPrevue,
        @NotBlank @Size(max = 255) String lieu
) {
}

