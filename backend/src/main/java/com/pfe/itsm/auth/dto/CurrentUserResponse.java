package com.pfe.itsm.auth.dto;

import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String nom,
        String prenom,
        String email,
        String telephone,
        UserRole role
) {

    public static CurrentUserResponse from(UserAccount user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getTelephone(),
                user.getRole()
        );
    }
}
