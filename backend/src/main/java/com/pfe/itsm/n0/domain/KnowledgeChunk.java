package com.pfe.itsm.n0.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(
        name = "knowledge_chunks",
        indexes = {
                @Index(name = "idx_knowledge_chunks_article", columnList = "article_id"),
                @Index(name = "idx_knowledge_chunks_active", columnList = "actif")
        }
)
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private KnowledgeArticle article;

    @Column(nullable = false, length = 4000)
    private String contenu;

    @Column(nullable = false, length = 1000)
    private String motsCles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KnowledgeSectionType sectionType = KnowledgeSectionType.AUTRE;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private int ordre;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(
            KnowledgeArticle article,
            String contenu,
            String motsCles,
            KnowledgeSectionType sectionType,
            int ordre,
            boolean actif
    ) {
        this.article = article;
        this.contenu = contenu;
        this.motsCles = motsCles;
        this.sectionType = sectionType;
        this.ordre = ordre;
        this.actif = actif;
    }

    public UUID getId() {
        return id;
    }

    public KnowledgeArticle getArticle() {
        return article;
    }

    public String getContenu() {
        return contenu;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public KnowledgeSectionType getSectionType() {
        return sectionType;
    }

    public boolean isActif() {
        return actif;
    }

    public int getOrdre() {
        return ordre;
    }
}
