package com.pfe.itsm.inventory.domain;

import com.pfe.itsm.common.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "spare_parts",
        indexes = {
                @Index(name = "idx_spare_parts_reference", columnList = "reference", unique = true),
                @Index(name = "idx_spare_parts_active", columnList = "actif")
        }
)
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String reference;

    @Column(nullable = false, length = 180)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int quantiteDisponible;

    @Column(nullable = false)
    private int seuilAlerte;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    @Column(nullable = false)
    private Instant dateDerniereModification = Instant.now();

    protected SparePart() {
    }

    public SparePart(String reference, String nom, String description, int quantiteDisponible, int seuilAlerte) {
        if (quantiteDisponible < 0 || seuilAlerte < 0) {
            throw new BusinessException("Les quantites de stock ne peuvent pas etre negatives.");
        }
        this.reference = reference;
        this.nom = nom;
        this.description = description;
        this.quantiteDisponible = quantiteDisponible;
        this.seuilAlerte = seuilAlerte;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La quantite doit etre positive.");
        }
        this.quantiteDisponible += quantity;
        touch();
    }

    public void consumeStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La quantite doit etre positive.");
        }
        if (this.quantiteDisponible < quantity) {
            throw new BusinessException("Stock insuffisant pour cette piece.");
        }
        this.quantiteDisponible -= quantity;
        touch();
    }

    public void adjustStock(int newQuantity) {
        if (newQuantity < 0) {
            throw new BusinessException("La quantite ajustee ne peut pas etre negative.");
        }
        this.quantiteDisponible = newQuantity;
        touch();
    }

    public void update(String nom, String description, int seuilAlerte, boolean actif) {
        if (seuilAlerte < 0) {
            throw new BusinessException("Le seuil d'alerte ne peut pas etre negatif.");
        }
        this.nom = nom;
        this.description = description;
        this.seuilAlerte = seuilAlerte;
        this.actif = actif;
        touch();
    }

    public boolean isLowStock() {
        return actif && quantiteDisponible <= seuilAlerte;
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

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public boolean isActif() {
        return actif;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }
}

