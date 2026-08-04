package com.pfe.itsm.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveTicketRequest(
        @NotBlank @Size(max = 1000) String commentaire
) {
}
