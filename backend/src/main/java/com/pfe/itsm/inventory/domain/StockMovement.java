package com.pfe.itsm.inventory.domain;

import com.pfe.itsm.interventions.domain.Intervention;
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
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movements_piece", columnList = "piece_id"),
                @Index(name = "idx_stock_movements_intervention", columnList = "intervention_id"),
                @Index(name = "idx_stock_movements_date", columnList = "date_mouvement")
        }
)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piece_id", nullable = false)
    private SparePart piece;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType type;

    @Column(nullable = false)
    private int quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervention_id")
    private Intervention intervention;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technicien_id", nullable = false)
    private UserAccount technicien;

    @Column(nullable = false, length = 1000)
    private String commentaire;

    @Column(nullable = false, updatable = false)
    private Instant dateMouvement = Instant.now();

    protected StockMovement() {
    }

    public StockMovement(
            SparePart piece,
            StockMovementType type,
            int quantite,
            Intervention intervention,
            UserAccount technicien,
            String commentaire
    ) {
        this.piece = piece;
        this.type = type;
        this.quantite = quantite;
        this.intervention = intervention;
        this.technicien = technicien;
        this.commentaire = commentaire;
    }

    public UUID getId() {
        return id;
    }

    public SparePart getPiece() {
        return piece;
    }

    public StockMovementType getType() {
        return type;
    }

    public int getQuantite() {
        return quantite;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public UserAccount getTechnicien() {
        return technicien;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getDateMouvement() {
        return dateMouvement;
    }
}

