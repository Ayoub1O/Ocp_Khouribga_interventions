package com.pfe.itsm.tickets.dto;

import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank @Size(max = 180) String titre,
        @NotBlank @Size(max = 4000) String description,
        @NotNull TicketCategory categorie,
        @NotNull TicketPriority priorite,
        @NotNull UUID demandeurId
) {
}

