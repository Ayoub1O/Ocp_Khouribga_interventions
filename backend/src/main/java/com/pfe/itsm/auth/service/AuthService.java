package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.dto.CurrentUserResponse;
import com.pfe.itsm.auth.dto.ChangePasswordRequest;
import com.pfe.itsm.auth.dto.ForgotPasswordRequest;
import com.pfe.itsm.auth.dto.RegisterRequest;
import com.pfe.itsm.auth.dto.LoginRequest;
import com.pfe.itsm.auth.dto.LoginResponse;
import com.pfe.itsm.auth.dto.ResetPasswordRequest;
import com.pfe.itsm.auth.dto.TokenPairResponse;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.users.domain.UserRole;
import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.auth.security.JwtService;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.repository.UserAccountRepository;
import com.pfe.itsm.users.service.PasswordPolicy;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CurrentUserService currentUserService;
    private final EmailVerificationService emailVerificationService;
    private final UserInvitationService userInvitationService;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            CurrentUserService currentUserService,
            EmailVerificationService emailVerificationService,
            UserInvitationService userInvitationService,
            PasswordResetService passwordResetService,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy
    ) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.currentUserService = currentUserService;
        this.emailVerificationService = emailVerificationService;
        this.userInvitationService = userInvitationService;
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmail(email)) {
            throw new BusinessException("Un utilisateur existe deja avec cet email.");
        }
        passwordPolicy.validate(request.password());

        UserAccount user = userAccountRepository.save(new UserAccount(
                request.nom().trim(),
                request.prenom().trim(),
                email,
                normalizeOptional(request.telephone()),
                passwordEncoder.encode(request.password()),
                UserRole.DEMANDEUR,
                true,
                false
        ));
        emailVerificationService.issue(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verify(token);
    }

    @Transactional
    public void acceptInvitation(String token, String password) {
        passwordPolicy.validate(password);
        UserAccount user = userInvitationService.accept(token);
        user.changerMotDePasse(passwordEncoder.encode(password));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password())
        );

        UserAccount user = userAccountRepository.findByEmail(normalizeEmail(request.email())).orElseThrow();
        JwtService.TokenResult token = jwtService.generateAccessToken(user);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);
        return new LoginResponse(
                token.token(),
                refreshToken.token(),
                "Bearer",
                token.expiresAt(),
                refreshToken.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getTelephone(),
                user.getRole()
        );
    }

    @Transactional
    public TokenPairResponse refresh(String rawRefreshToken) {
        UserAccount user = refreshTokenService.consumeAndRotateSubject(rawRefreshToken);
        JwtService.TokenResult accessToken = jwtService.generateAccessToken(user);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);
        return new TokenPairResponse(
                accessToken.token(),
                refreshToken.token(),
                "Bearer",
                accessToken.expiresAt(),
                refreshToken.expiresAt()
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.reset(request.token(), request.newPassword());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        passwordResetService.changeAuthenticatedPassword(request.currentPassword(), request.newPassword());
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me() {
        return CurrentUserResponse.from(currentUserService.currentUser());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
