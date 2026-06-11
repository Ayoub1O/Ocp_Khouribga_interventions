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
        name = "knowledge_sections",
        indexes = {
                @Index(name = "idx_knowledge_sections_article", columnList = "article_id"),
                @Index(name = "idx_knowledge_sections_type", columnList = "type")
        }
)
public class KnowledgeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private KnowledgeArticle article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KnowledgeSectionType type;

    @Column(nullable = false, length = 180)
    private String titre;

    @Column(nullable = false, length = 4000)
    private String contenu;

    @Column(nullable = false)
    private int ordre;

    protected KnowledgeSection() {
    }

    public KnowledgeSection(
            KnowledgeArticle article,
            KnowledgeSectionType type,
            String titre,
            String contenu,
            int ordre
    ) {
        this.article = article;
        this.type = type;
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
    }

    public UUID getId() {
        return id;
    }

    public KnowledgeArticle getArticle() {
        return article;
    }

    public KnowledgeSectionType getType() {
        return type;
    }

    public String getTitre() {
        return titre;
    }

    public String getContenu() {
        return contenu;
    }

    public int getOrdre() {
        return ordre;
    }
}
