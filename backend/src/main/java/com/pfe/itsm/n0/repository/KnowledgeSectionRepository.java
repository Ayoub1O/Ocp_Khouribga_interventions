package com.pfe.itsm.n0.repository;

import com.pfe.itsm.n0.domain.KnowledgeSection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeSectionRepository extends JpaRepository<KnowledgeSection, UUID> {

    void deleteByArticleId(UUID articleId);
}
