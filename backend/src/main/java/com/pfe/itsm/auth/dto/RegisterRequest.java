package com.pfe.itsm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @Email @NotBlank @Size(max = 180) String email,
        @NotBlank @Size(min = 12, max = 128) String password
) {
}

