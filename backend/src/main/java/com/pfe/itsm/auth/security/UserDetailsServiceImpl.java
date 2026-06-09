package com.pfe.itsm.auth.security;

import com.pfe.itsm.auth.domain.AuthenticatedUser;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserDetailsServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userAccountRepository.findByEmail(username.trim().toLowerCase(Locale.ROOT))
                .map(AuthenticatedUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides."));
    }
}
