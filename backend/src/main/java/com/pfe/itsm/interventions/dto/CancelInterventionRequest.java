package com.pfe.itsm.interventions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelInterventionRequest(
        @NotBlank @Size(max = 4000) String raison
) {
}

