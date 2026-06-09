package com.pfe.itsm.interventions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteInterventionRequest(
        @NotBlank @Size(max = 4000) String rapport
) {
}

