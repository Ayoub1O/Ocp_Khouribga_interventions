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
        name = "tickets",
        indexes = {
                @Index(name = "idx_tickets_status_level", columnList = "statut,niveau_courant"),
                @Index(name = "idx_tickets_demandeur", columnList = "demandeur_id"),
                @Index(name = "idx_tickets_technicien", columnList = "technicien_assigne_id")
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @Column(nullable = false, length = 180)
    private String titre;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketPriority priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus statut = TicketStatus.OUVERT;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_courant", nullable = false, length = 10)
    private SupportLevel niveauCourant = SupportLevel.N1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private UserAccount demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_assigne_id")
    private UserAccount technicienAssigne;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    @Column(nullable = false)
    private Instant dateDerniereModification = Instant.now();

    private Instant dateResolution;

    private Instant dateCloture;

    protected Ticket() {
    }

    public Ticket(
            String reference,
            String titre,
            String description,
            TicketCategory categorie,
            TicketPriority priorite,
            UserAccount demandeur
    ) {
        this.reference = reference;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.priorite = priorite;
        this.demandeur = demandeur;
    }

    public void claim(UserAccount technicien) {
        if (statut == TicketStatus.RESOLU || statut == TicketStatus.CLOTURE) {
            throw new IllegalStateException("Un ticket resolu ou cloture ne peut pas etre pris en charge.");
        }
        if (technicienAssigne != null) {
            throw new IllegalStateException("Ce ticket est deja pris en charge.");
        }
        this.technicienAssigne = technicien;
        this.statut = TicketStatus.EN_COURS;
        touch();
    }

    public void escalate(SupportLevel nextLevel) {
        if (statut == TicketStatus.CLOTURE) {
            throw new IllegalStateException("Un ticket cloture ne peut pas etre escalade.");
        }
        this.niveauCourant = nextLevel;
        this.technicienAssigne = null;
        this.statut = TicketStatus.ESCALADE;
        touch();
    }

    public void resolve() {
        if (statut == TicketStatus.CLOTURE) {
            throw new IllegalStateException("Un ticket cloture ne peut pas etre resolu a nouveau.");
        }
        this.statut = TicketStatus.RESOLU;
        this.dateResolution = Instant.now();
        touch();
    }

    public void close() {
        if (statut != TicketStatus.RESOLU) {
            throw new IllegalStateException("Seul un ticket resolu peut etre cloture.");
        }
        this.statut = TicketStatus.CLOTURE;
        this.dateCloture = Instant.now();
        touch();
    }

    private void touch() {
        this.dateDerniereModification = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public TicketCategory getCategorie() {
        return categorie;
    }

    public TicketPriority getPriorite() {
        return priorite;
    }

    public TicketStatus getStatut() {
        return statut;
    }

    public SupportLevel getNiveauCourant() {
        return niveauCourant;
    }

    public UserAccount getDemandeur() {
        return demandeur;
    }

    public UserAccount getTechnicienAssigne() {
        return technicienAssigne;
    }
}
