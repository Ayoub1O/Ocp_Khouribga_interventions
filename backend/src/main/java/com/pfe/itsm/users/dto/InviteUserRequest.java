package com.pfe.itsm.users.dto;

import com.pfe.itsm.users.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteUserRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @Email @NotBlank @Size(max = 180) String email,
        @Size(max = 40) String telephone,
        @NotNull UserRole role
) {
}
