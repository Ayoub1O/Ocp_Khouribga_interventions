package com.pfe.itsm.n0.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SparqlQueryRequest(
        @NotBlank @Size(max = 4000) String query
) {
}
