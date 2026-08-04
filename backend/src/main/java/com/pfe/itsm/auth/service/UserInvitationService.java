package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.config.MailProperties;
import com.pfe.itsm.auth.config.OnboardingProperties;
import com.pfe.itsm.auth.domain.UserInvitation;
import com.pfe.itsm.auth.repository.UserInvitationRepository;
import com.pfe.itsm.auth.security.MessageDigestSupport;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserInvitationService {

    private final UserInvitationRepository invitationRepository;
    private final SecureTokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final MailProperties mailProperties;
    private final OnboardingProperties onboardingProperties;
    private final Clock clock = Clock.systemUTC();

    public UserInvitationService(
            UserInvitationRepository invitationRepository,
            SecureTokenGenerator tokenGenerator,
            EmailService emailService,
            MailProperties mailProperties,
            OnboardingProperties onboardingProperties
    ) {
        this.invitationRepository = invitationRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.mailProperties = mailProperties;
        this.onboardingProperties = onboardingProperties;
    }

    @Transactional
    public void issue(UserAccount invitedUser, UserRole invitedRole, UserAccount invitedBy) {
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = Instant.now(clock)
                .plus(onboardingProperties.invitationExpirationHours(), ChronoUnit.HOURS);
        invitationRepository.save(new UserInvitation(
                MessageDigestSupport.sha256Base64Url(rawToken),
                invitedUser,
                invitedRole,
                invitedBy,
                expiresAt
        ));
        emailService.sendInvitation(invitedUser.getEmail(), invitationLink(rawToken));
    }

    @Transactional
    public UserAccount accept(String rawToken) {
        UserInvitation invitation = invitationRepository.findByTokenHash(MessageDigestSupport.sha256Base64Url(rawToken))
                .orElseThrow(() -> new BusinessException("Invitation invalide."));
        if (!invitation.isUsable(Instant.now(clock))) {
            throw new BusinessException("Invitation invalide ou expiree.");
        }

        UserAccount user = invitation.getUser();
        if (user.getRole() != UserRole.DEMANDEUR && user.getRole() != invitation.getInvitedRole()) {
            throw new BusinessException("Invitation incoherente avec le role utilisateur.");
        }
        invitation.accept();
        user.changerRole(invitation.getInvitedRole());
        user.verifierEmail();
        user.activer();
        return user;
    }

    private String invitationLink(String rawToken) {
        return mailProperties.frontendBaseUrl() + "/accept-invitation?token=" + rawToken;
    }
}
