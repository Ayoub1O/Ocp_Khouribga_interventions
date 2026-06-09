package com.pfe.itsm.interventions.domain;

import com.pfe.itsm.tickets.domain.Ticket;
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
        name = "interventions",
        indexes = {
                @Index(name = "idx_interventions_ticket", columnList = "ticket_id"),
                @Index(name = "idx_interventions_technicien", columnList = "technicien_id"),
                @Index(name = "idx_interventions_status", columnList = "statut"),
                @Index(name = "idx_interventions_planning", columnList = "date_debut_prevue,date_fin_prevue")
        }
)
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technicien_id", nullable = false)
    private UserAccount technicien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterventionStatus statut = InterventionStatus.PLANIFIEE;

    @Column(nullable = false)
    private Instant dateDebutPrevue;

    @Column(nullable = false)
    private Instant dateFinPrevue;

    private Instant dateDebutReelle;

    private Instant dateFinReelle;

    @Column(nullable = false, length = 255)
    private String lieu;

    @Column(length = 4000)
    private String rapport;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    @Column(nullable = false)
    private Instant dateDerniereModification = Instant.now();

    protected Intervention() {
    }

    public Intervention(
            Ticket ticket,
            UserAccount technicien,
            Instant dateDebutPrevue,
            Instant dateFinPrevue,
            String lieu
    ) {
        this.ticket = ticket;
        this.technicien = technicien;
        this.dateDebutPrevue = dateDebutPrevue;
        this.dateFinPrevue = dateFinPrevue;
        this.lieu = lieu;
    }

    public void start() {
        if (statut != InterventionStatus.PLANIFIEE) {
            throw new IllegalStateException("Seule une intervention planifiee peut demarrer.");
        }
        this.statut = InterventionStatus.EN_COURS;
        this.dateDebutReelle = Instant.now();
        touch();
    }

    public void complete(String rapport) {
        if (statut != InterventionStatus.EN_COURS) {
            throw new IllegalStateException("Seule une intervention en cours peut etre terminee.");
        }
        this.statut = InterventionStatus.TERMINEE;
        this.rapport = rapport;
        this.dateFinReelle = Instant.now();
        touch();
    }

    public void cancel(String raison) {
        if (statut == InterventionStatus.TERMINEE) {
            throw new IllegalStateException("Une intervention terminee ne peut pas etre annulee.");
        }
        this.statut = InterventionStatus.ANNULEE;
        this.rapport = raison;
        touch();
    }

    private void touch() {
        this.dateDerniereModification = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public UserAccount getTechnicien() {
        return technicien;
    }

    public InterventionStatus getStatut() {
        return statut;
    }

    public Instant getDateDebutPrevue() {
        return dateDebutPrevue;
    }

    public Instant getDateFinPrevue() {
        return dateFinPrevue;
    }

    public Instant getDateDebutReelle() {
        return dateDebutReelle;
    }

    public Instant getDateFinReelle() {
        return dateFinReelle;
    }

    public String getLieu() {
        return lieu;
    }

    public String getRapport() {
        return rapport;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }
}

