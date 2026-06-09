package com.pfe.itsm.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.onboarding")
public record OnboardingProperties(
        long emailVerificationExpirationHours,
        long invitationExpirationHours
) {
}

