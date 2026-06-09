package com.pfe.itsm.auth.dto;

import java.time.Instant;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}

