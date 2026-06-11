package com.pfe.itsm.n0.domain;

import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.users.domain.UserAccount;
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
        name = "chatbot_sessions",
        indexes = {
                @Index(name = "idx_chatbot_sessions_demandeur", columnList = "demandeur_id"),
                @Index(name = "idx_chatbot_sessions_status", columnList = "statut")
        }
)
public class ChatbotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private UserAccount demandeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChatbotSessionStatus statut = ChatbotSessionStatus.OUVERTE;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private TicketCategory categorieDetectee;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    private Instant dateFermeture;

    protected ChatbotSession() {
    }

    public ChatbotSession(UserAccount demandeur) {
        this.demandeur = demandeur;
    }

    public void updateCategory(TicketCategory categorie) {
        this.categorieDetectee = categorie;
    }

    public void markResolved() {
        this.statut = ChatbotSessionStatus.RESOLUE;
        this.dateFermeture = Instant.now();
    }

    public void markEscalated(UUID ticketId) {
        this.statut = ChatbotSessionStatus.ESCALADEE;
        this.ticketId = ticketId;
        this.dateFermeture = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getDemandeur() {
        return demandeur;
    }

    public ChatbotSessionStatus getStatut() {
        return statut;
    }

    public TicketCategory getCategorieDetectee() {
        return categorieDetectee;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Instant getDateFermeture() {
        return dateFermeture;
    }
}
