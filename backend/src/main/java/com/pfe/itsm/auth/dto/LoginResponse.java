package com.pfe.itsm.auth.dto;

import com.pfe.itsm.users.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UUID userId,
        String email,
        String telephone,
        UserRole role
) {
}
