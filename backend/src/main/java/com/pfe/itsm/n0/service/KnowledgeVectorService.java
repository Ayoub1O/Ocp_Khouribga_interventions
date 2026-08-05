package com.pfe.itsm.n0.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.n0.ai.LlmClient;
import com.pfe.itsm.n0.config.N0AiProperties;
import com.pfe.itsm.n0.domain.KnowledgeChunk;
import com.pfe.itsm.n0.dto.EmbeddingReindexResponse;
import com.pfe.itsm.n0.dto.VectorChunkMatch;
import com.pfe.itsm.n0.repository.KnowledgeChunkRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeVectorService {

    private final LlmClient llmClient;
    private final N0AiProperties properties;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KnowledgeChunkRepository chunkRepository;

    public KnowledgeVectorService(
            LlmClient llmClient,
            N0AiProperties properties,
            NamedParameterJdbcTemplate jdbcTemplate,
            KnowledgeChunkRepository chunkRepository
    ) {
        this.llmClient = llmClient;
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
        this.chunkRepository = chunkRepository;
    }

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public void indexChunk(KnowledgeChunk chunk) {
        if (!isAvailable()) {
            return;
        }
        List<Double> embedding = llmClient.embedForDocument(chunk.getArticle().getTitre(), chunk.getContenu());
        validateDimension(embedding);

        jdbcTemplate.update("""
                insert into knowledge_chunk_embeddings (
                    chunk_id,
                    embedding,
                    embedding_model,
                    embedding_dimension,
                    date_derniere_modification
                ) values (
                    :chunkId,
                    cast(:embedding as vector),
                    :model,
                    :dimension,
                    now()
                )
                on conflict (chunk_id) do update set
                    embedding = excluded.embedding,
                    embedding_model = excluded.embedding_model,
                    embedding_dimension = excluded.embedding_dimension,
                    date_derniere_modification = now()
                """, new MapSqlParameterSource()
                .addValue("chunkId", chunk.getId())
                .addValue("embedding", toVectorLiteral(embedding))
                .addValue("model", properties.embeddingModel())
                .addValue("dimension", properties.embeddingDimension()));
    }

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<VectorChunkMatch> search(String sanitizedQuestion) {
        if (!isAvailable() || sanitizedQuestion == null || sanitizedQuestion.isBlank()) {
            return List.of();
        }
        List<Double> embedding = llmClient.embedForQuery(sanitizedQuestion);
        validateDimension(embedding);

        List<VectorRow> rows = jdbcTemplate.query("""
                select kce.chunk_id, 1 - (kce.embedding <=> cast(:embedding as vector)) as score
                from knowledge_chunk_embeddings kce
                join knowledge_chunks kc on kc.id = kce.chunk_id
                join knowledge_articles ka on ka.id = kc.article_id
                where kc.actif = true and ka.actif = true
                order by kce.embedding <=> cast(:embedding as vector)
                limit :limit
                """, new MapSqlParameterSource()
                .addValue("embedding", toVectorLiteral(embedding))
                .addValue("limit", properties.vectorSearchLimit()), (rs, rowNum) -> new VectorRow(
                UUID.fromString(rs.getString("chunk_id")),
                rs.getDouble("score")
        ));

        Map<UUID, Double> scoreByChunkId = new LinkedHashMap<>();
        rows.forEach(row -> scoreByChunkId.put(row.chunkId(), row.score()));

        List<VectorChunkMatch> matches = new ArrayList<>();
        chunkRepository.findAllById(scoreByChunkId.keySet()).forEach(chunk ->
                matches.add(new VectorChunkMatch(chunk, scoreByChunkId.get(chunk.getId())))
        );
        matches.sort((left, right) -> Double.compare(right.score(), left.score()));
        return matches;
    }

    @Transactional
    public EmbeddingReindexResponse reindexActiveChunks() {
        List<KnowledgeChunk> chunks = chunkRepository.findByActifTrueAndArticleActifTrue();
        if (!isAvailable()) {
            return new EmbeddingReindexResponse(chunks.size(), 0, 0, false);
        }

        int generated = 0;
        int errors = 0;
        for (KnowledgeChunk chunk : chunks) {
            try {
                indexChunk(chunk);
                generated++;
            } catch (RuntimeException exception) {
                errors++;
            }
        }
        return new EmbeddingReindexResponse(chunks.size(), generated, errors, true);
    }

    private void validateDimension(List<Double> embedding) {
        if (embedding.size() != properties.embeddingDimension()) {
            throw new BusinessException("Dimension d'embedding invalide: attendu "
                    + properties.embeddingDimension()
                    + ", recu "
                    + embedding.size()
                    + ".");
        }
    }

    private String toVectorLiteral(List<Double> embedding) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(embedding.get(i));
        }
        return builder.append(']').toString();
    }

    private record VectorRow(UUID chunkId, double score) {
    }
}
