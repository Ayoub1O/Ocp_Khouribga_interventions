package com.pfe.itsm.n0.dto;

import com.pfe.itsm.n0.domain.KnowledgeArticle;
import com.pfe.itsm.tickets.domain.TicketCategory;
import java.time.Instant;
import java.util.UUID;

public record KnowledgeArticleResponse(
        UUID id,
        String titre,
        TicketCategory categorie,
        String contenu,
        String motsCles,
        String sourceType,
        String sourceNom,
        boolean actif,
        int version,
        Instant dateCreation,
        Instant dateDerniereModification
) {

    public static KnowledgeArticleResponse from(KnowledgeArticle article) {
        return new KnowledgeArticleResponse(
                article.getId(),
                article.getTitre(),
                article.getCategorie(),
                article.getContenu(),
                article.getMotsCles(),
                article.getSourceType(),
                article.getSourceNom(),
                article.isActif(),
                article.getVersion(),
                article.getDateCreation(),
                article.getDateDerniereModification()
        );
    }
}
