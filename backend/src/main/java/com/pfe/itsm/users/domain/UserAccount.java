package com.pfe.itsm.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    protected UserAccount() {
    }

    public UserAccount(String nom, String prenom, String email, String passwordHash, UserRole role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UserAccount(
            String nom,
            String prenom,
            String email,
            String passwordHash,
            UserRole role,
            boolean actif,
            boolean emailVerified
    ) {
        this(nom, prenom, email, passwordHash, role);
        this.actif = actif;
        this.emailVerified = emailVerified;
    }

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActif() {
        return actif;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void desactiver() {
        this.actif = false;
    }

    public void activer() {
        this.actif = true;
    }

    public void verifierEmail() {
        this.emailVerified = true;
    }

    public void changerMotDePasse(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
