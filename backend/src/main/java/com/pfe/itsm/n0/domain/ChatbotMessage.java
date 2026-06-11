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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chatbot_messages",
        indexes = {
                @Index(name = "idx_chatbot_messages_session", columnList = "session_id"),
                @Index(name = "idx_chatbot_messages_date", columnList = "date_creation")
        }
)
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatbotSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChatbotMessageAuthor auteur;

    @Column(nullable = false, length = 4000)
    private String contenu;

    @Column(length = 2000)
    private String sourcesUtilisees;

    private Double confidenceScore;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    protected ChatbotMessage() {
    }

    public ChatbotMessage(
            ChatbotSession session,
            ChatbotMessageAuthor auteur,
            String contenu,
            String sourcesUtilisees,
            Double confidenceScore
    ) {
        this.session = session;
        this.auteur = auteur;
        this.contenu = contenu;
        this.sourcesUtilisees = sourcesUtilisees;
        this.confidenceScore = confidenceScore;
    }

    public UUID getId() {
        return id;
    }

    public ChatbotSession getSession() {
        return session;
    }

    public ChatbotMessageAuthor getAuteur() {
        return auteur;
    }

    public String getContenu() {
        return contenu;
    }

    public String getSourcesUtilisees() {
        return sourcesUtilisees;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }
}
