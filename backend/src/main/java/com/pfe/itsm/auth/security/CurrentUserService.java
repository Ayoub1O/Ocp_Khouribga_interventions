package com.pfe.itsm.auth.security;

import com.pfe.itsm.auth.domain.AuthenticatedUser;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResourceNotFoundException("Utilisateur authentifie introuvable.");
        }
        return user.getId();
    }

    public UserAccount currentUser() {
        return userAccountRepository.findById(currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur authentifie introuvable."));
    }
}

