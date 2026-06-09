package com.pfe.itsm.auth.domain;

import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
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
        name = "user_invitations",
        indexes = {
                @Index(name = "idx_user_invitations_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_user_invitations_user", columnList = "user_id"),
                @Index(name = "idx_user_invitations_invited_by", columnList = "invited_by_id")
        }
)
public class UserInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole invitedRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private UserAccount invitedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant acceptedAt;

    private Instant revokedAt;

    protected UserInvitation() {
    }

    public UserInvitation(
            String tokenHash,
            UserAccount user,
            UserRole invitedRole,
            UserAccount invitedBy,
            Instant expiresAt
    ) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.invitedRole = invitedRole;
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return acceptedAt == null && revokedAt == null && expiresAt.isAfter(now);
    }

    public void accept() {
        this.acceptedAt = Instant.now();
    }

    public UserAccount getUser() {
        return user;
    }

    public UserRole getInvitedRole() {
        return invitedRole;
    }
}

