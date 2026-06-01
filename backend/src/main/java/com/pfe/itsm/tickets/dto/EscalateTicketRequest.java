package com.pfe.itsm.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EscalateTicketRequest(
        UUID acteurId,
        @NotBlank @Size(max = 1000) String raison
) {
}

