package com.pfe.itsm.n0.dto;

import com.pfe.itsm.n0.domain.KnowledgeChunk;

public record VectorChunkMatch(
        KnowledgeChunk chunk,
        double score
) {
}
