package com.pfe.itsm.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CloseTicketRequest(
        @NotBlank @Size(max = 1000) String commentaire
) {
}
