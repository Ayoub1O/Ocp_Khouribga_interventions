package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.config.MailProperties;
import com.pfe.itsm.auth.config.OnboardingProperties;
import com.pfe.itsm.auth.domain.EmailVerificationToken;
import com.pfe.itsm.auth.repository.EmailVerificationTokenRepository;
import com.pfe.itsm.auth.security.MessageDigestSupport;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.users.domain.UserAccount;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final SecureTokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final MailProperties mailProperties;
    private final OnboardingProperties onboardingProperties;
    private final Clock clock = Clock.systemUTC();

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            SecureTokenGenerator tokenGenerator,
            EmailService emailService,
            MailProperties mailProperties,
            OnboardingProperties onboardingProperties
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.mailProperties = mailProperties;
        this.onboardingProperties = onboardingProperties;
    }

    @Transactional
    public void issue(UserAccount user) {
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = Instant.now(clock)
                .plus(onboardingProperties.emailVerificationExpirationHours(), ChronoUnit.HOURS);
        tokenRepository.save(new EmailVerificationToken(
                MessageDigestSupport.sha256Base64Url(rawToken),
                user,
                expiresAt
        ));
        emailService.sendEmailVerification(user.getEmail(), verificationLink(rawToken));
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByTokenHash(MessageDigestSupport.sha256Base64Url(rawToken))
                .orElseThrow(() -> new BusinessException("Token de verification invalide."));
        if (!token.isUsable(Instant.now(clock))) {
            throw new BusinessException("Token de verification invalide ou expire.");
        }

        UserAccount user = token.getUser();
        user.verifierEmail();
        user.activer();
        token.consume();
    }

    private String verificationLink(String rawToken) {
        return mailProperties.frontendBaseUrl() + "/verify-email?token=" + rawToken;
    }
}

