package com.pfe.itsm.n0.dto;

public record KnowledgeImportResponse(
        KnowledgeArticleResponse article,
        int chunksGeneres,
        String message
) {
}
