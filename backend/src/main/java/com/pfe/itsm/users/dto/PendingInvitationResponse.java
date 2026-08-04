package com.pfe.itsm.users.dto;

import com.pfe.itsm.auth.domain.UserInvitation;
import com.pfe.itsm.users.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record PendingInvitationResponse(
        UUID id,
        UUID userId,
        String nom,
        String prenom,
        String email,
        String telephone,
        UserRole invitedRole,
        String invitedBy,
        Instant createdAt,
        Instant expiresAt
) {

    public static PendingInvitationResponse from(UserInvitation invitation) {
        return new PendingInvitationResponse(
                invitation.getId(),
                invitation.getUser().getId(),
                invitation.getUser().getNom(),
                invitation.getUser().getPrenom(),
                invitation.getUser().getEmail(),
                invitation.getUser().getTelephone(),
                invitation.getInvitedRole(),
                invitation.getInvitedBy().getPrenom() + " " + invitation.getInvitedBy().getNom(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt()
        );
    }
}
