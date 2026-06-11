package com.pfe.itsm.n0.domain;

import com.pfe.itsm.tickets.domain.TicketCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "knowledge_articles",
        indexes = {
                @Index(name = "idx_knowledge_articles_category", columnList = "categorie"),
                @Index(name = "idx_knowledge_articles_active", columnList = "actif")
        }
)
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory categorie;

    @Column(nullable = false, length = 4000)
    private String contenu;

    @Column(nullable = false, length = 1000)
    private String motsCles;

    @Column(nullable = false, length = 30)
    private String sourceType = "MANUEL";

    @Column(length = 255)
    private String sourceNom;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    @Column(nullable = false)
    private Instant dateDerniereModification = Instant.now();

    protected KnowledgeArticle() {
    }

    public KnowledgeArticle(String titre, TicketCategory categorie, String contenu, String motsCles, boolean actif) {
        this.titre = titre;
        this.categorie = categorie;
        this.contenu = contenu;
        this.motsCles = motsCles;
        this.actif = actif;
    }

    public KnowledgeArticle(
            String titre,
            TicketCategory categorie,
            String contenu,
            String motsCles,
            boolean actif,
            String sourceType,
            String sourceNom
    ) {
        this(titre, categorie, contenu, motsCles, actif);
        this.sourceType = sourceType;
        this.sourceNom = sourceNom;
    }

    public void update(String titre, TicketCategory categorie, String contenu, String motsCles, boolean actif) {
        this.titre = titre;
        this.categorie = categorie;
        this.contenu = contenu;
        this.motsCles = motsCles;
        this.actif = actif;
        this.version++;
        this.dateDerniereModification = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public TicketCategory getCategorie() {
        return categorie;
    }

    public String getContenu() {
        return contenu;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public boolean isActif() {
        return actif;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceNom() {
        return sourceNom;
    }

    public int getVersion() {
        return version;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Instant getDateDerniereModification() {
        return dateDerniereModification;
    }
}
