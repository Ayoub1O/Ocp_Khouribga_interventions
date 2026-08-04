package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.config.MailProperties;
import com.pfe.itsm.auth.config.OnboardingProperties;
import com.pfe.itsm.auth.domain.PasswordResetToken;
import com.pfe.itsm.auth.repository.PasswordResetTokenRepository;
import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.auth.security.MessageDigestSupport;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.repository.UserAccountRepository;
import com.pfe.itsm.users.service.PasswordPolicy;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final SecureTokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final MailProperties mailProperties;
    private final OnboardingProperties onboardingProperties;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock = Clock.systemUTC();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserAccountRepository userAccountRepository,
            SecureTokenGenerator tokenGenerator,
            EmailService emailService,
            MailProperties mailProperties,
            OnboardingProperties onboardingProperties,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            RefreshTokenService refreshTokenService
    ) {
        this.tokenRepository = tokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.mailProperties = mailProperties;
        this.onboardingProperties = onboardingProperties;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void requestReset(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        userAccountRepository.findByEmail(normalizedEmail)
                .filter(UserAccount::isActif)
                .filter(UserAccount::isEmailVerified)
                .ifPresent(this::issue);
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        passwordPolicy.validate(newPassword);
        PasswordResetToken token = tokenRepository.findByTokenHash(MessageDigestSupport.sha256Base64Url(rawToken))
                .orElseThrow(() -> new BusinessException("Token de reinitialisation invalide."));
        if (!token.isUsable(Instant.now(clock))) {
            throw new BusinessException("Token de reinitialisation invalide ou expire.");
        }

        UserAccount user = token.getUser();
        user.changerMotDePasse(passwordEncoder.encode(newPassword));
        token.consume();
        refreshTokenService.revokeAllFor(user);
    }

    @Transactional
    public void changeAuthenticatedPassword(String currentPassword, String newPassword) {
        UserAccount user = currentUserService.currentUser();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Mot de passe actuel incorrect.");
        }
        passwordPolicy.validate(newPassword);
        user.changerMotDePasse(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAllFor(user);
    }

    private void issue(UserAccount user) {
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = Instant.now(clock)
                .plus(onboardingProperties.passwordResetExpirationMinutes(), ChronoUnit.MINUTES);
        tokenRepository.save(new PasswordResetToken(
                MessageDigestSupport.sha256Base64Url(rawToken),
                user,
                expiresAt
        ));
        emailService.sendPasswordReset(user.getEmail(), resetLink(rawToken));
    }

    private String resetLink(String rawToken) {
        return mailProperties.frontendBaseUrl() + "/reset-password?token=" + rawToken;
    }
}
