package com.pfe.itsm.n0.repository;

import com.pfe.itsm.n0.domain.KnowledgeChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {

    List<KnowledgeChunk> findByActifTrueAndArticleActifTrue();

    void deleteByArticleId(UUID articleId);
}
