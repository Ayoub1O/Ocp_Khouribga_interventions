package com.pfe.itsm.users.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.auth.service.SecureTokenGenerator;
import com.pfe.itsm.auth.service.UserInvitationService;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import com.pfe.itsm.users.dto.CreateUserRequest;
import com.pfe.itsm.users.dto.InviteUserRequest;
import com.pfe.itsm.users.dto.UpdateProfileRequest;
import com.pfe.itsm.users.dto.UserResponse;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final CurrentUserService currentUserService;
    private final UserInvitationService userInvitationService;
    private final SecureTokenGenerator secureTokenGenerator;

    public UserService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            CurrentUserService currentUserService,
            UserInvitationService userInvitationService,
            SecureTokenGenerator secureTokenGenerator
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.currentUserService = currentUserService;
        this.userInvitationService = userInvitationService;
        this.secureTokenGenerator = secureTokenGenerator;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmail(email)) {
            throw new BusinessException("Un utilisateur existe deja avec cet email.");
        }
        passwordPolicy.validate(request.password());

        UserAccount user = new UserAccount(
                request.nom().trim(),
                request.prenom().trim(),
                email,
                normalizeOptional(request.telephone()),
                passwordEncoder.encode(request.password()),
                request.role(),
                true,
                true
        );
        return UserResponse.from(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse invite(InviteUserRequest request) {
        if (request.role() == UserRole.DEMANDEUR) {
            throw new BusinessException("Les demandeurs doivent utiliser l'inscription publique.");
        }

        String email = normalizeEmail(request.email());
        UserAccount user = userAccountRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (user.getRole() != UserRole.DEMANDEUR) {
                throw new BusinessException("Ce compte possede deja un role interne.");
            }
            userInvitationService.issue(user, request.role(), currentUserService.currentUser());
            return UserResponse.from(user);
        }

        UserAccount invitedUser = userAccountRepository.save(new UserAccount(
                request.nom().trim(),
                request.prenom().trim(),
                email,
                normalizeOptional(request.telephone()),
                passwordEncoder.encode(secureTokenGenerator.generate()),
                request.role(),
                false,
                false
        ));
        userInvitationService.issue(invitedUser, request.role(), currentUserService.currentUser());
        return UserResponse.from(invitedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userAccountRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return userAccountRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    @Transactional
    public UserResponse updateCurrentProfile(UpdateProfileRequest request) {
        UserAccount user = currentUserService.currentUser();
        user.mettreAJourProfil(
                request.nom().trim(),
                request.prenom().trim(),
                normalizeOptional(request.telephone())
        );
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
