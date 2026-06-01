package com.pfe.itsm.tickets.domain;

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
        name = "ticket_events",
        indexes = {
                @Index(name = "idx_ticket_events_ticket", columnList = "ticket_id"),
                @Index(name = "idx_ticket_events_date", columnList = "date_evenement")
        }
)
public class TicketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acteur_id")
    private UserAccount acteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketEventType type;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "date_evenement", nullable = false, updatable = false)
    private Instant dateEvenement = Instant.now();

    protected TicketEvent() {
    }

    public TicketEvent(Ticket ticket, UserAccount acteur, TicketEventType type, String commentaire) {
        this.ticket = ticket;
        this.acteur = acteur;
        this.type = type;
        this.commentaire = commentaire;
    }

    public UUID getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public UserAccount getActeur() {
        return acteur;
    }

    public TicketEventType getType() {
        return type;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getDateEvenement() {
        return dateEvenement;
    }
}

