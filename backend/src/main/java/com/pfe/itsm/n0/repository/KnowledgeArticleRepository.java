package com.pfe.itsm.n0.repository;

import com.pfe.itsm.n0.domain.KnowledgeArticle;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, UUID> {

    List<KnowledgeArticle> findByActifTrue();
}
