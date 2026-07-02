package com.pfe.itsm.n0.dto;

public record EmbeddingReindexResponse(
        int chunksTraites,
        int embeddingsGeneres,
        int erreurs,
        boolean fournisseurConfigure
) {
}
