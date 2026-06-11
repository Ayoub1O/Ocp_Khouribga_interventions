package com.pfe.itsm.n0.dto;

import com.pfe.itsm.tickets.domain.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KnowledgeArticleRequest(
        @NotBlank @Size(max = 180) String titre,
        @NotNull TicketCategory categorie,
        @NotBlank @Size(max = 4000) String contenu,
        @NotBlank @Size(max = 1000) String motsCles,
        boolean actif
) {
}
