package com.pfe.itsm.users.dto;

import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nom,
        String prenom,
        String email,
        UserRole role,
        boolean actif,
        boolean emailVerified,
        Instant dateCreation
) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.isActif(),
                user.isEmailVerified(),
                user.getDateCreation()
        );
    }
}
