package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.config.JwtProperties;
import com.pfe.itsm.auth.domain.RefreshToken;
import com.pfe.itsm.auth.repository.RefreshTokenRepository;
import com.pfe.itsm.auth.security.MessageDigestSupport;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.users.domain.UserAccount;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureTokenGenerator secureTokenGenerator;
    private final Clock clock = Clock.systemUTC();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties,
            SecureTokenGenerator secureTokenGenerator
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.secureTokenGenerator = secureTokenGenerator;
    }

    @Transactional
    public IssuedRefreshToken issue(UserAccount user) {
        Instant expiresAt = Instant.now(clock).plus(jwtProperties.refreshExpirationDays(), ChronoUnit.DAYS);
        String rawToken = secureTokenGenerator.generate();
        refreshTokenRepository.save(new RefreshToken(MessageDigestSupport.sha256Base64Url(rawToken), user, expiresAt));
        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Transactional
    public UserAccount consumeAndRotateSubject(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(MessageDigestSupport.sha256Base64Url(rawToken))
                .orElseThrow(() -> new BusinessException("Refresh token invalide."));
        if (!refreshToken.isUsable(Instant.now(clock)) || !refreshToken.getUser().isActif()) {
            throw new BusinessException("Refresh token invalide ou expire.");
        }
        refreshToken.revoke();
        return refreshToken.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(MessageDigestSupport.sha256Base64Url(rawToken))
                .ifPresent(RefreshToken::revoke);
    }
    public record IssuedRefreshToken(String token, Instant expiresAt) {
    }
}
