package com.pfe.itsm.auth.controller;

import com.pfe.itsm.auth.dto.AcceptInvitationRequest;
import com.pfe.itsm.auth.dto.CurrentUserResponse;
import com.pfe.itsm.auth.dto.LoginRequest;
import com.pfe.itsm.auth.dto.LoginResponse;
import com.pfe.itsm.auth.dto.RegisterRequest;
import com.pfe.itsm.auth.dto.RefreshTokenRequest;
import com.pfe.itsm.auth.dto.TokenPairResponse;
import com.pfe.itsm.auth.service.AuthService;
import com.pfe.itsm.common.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("Compte cree. Veuillez verifier votre adresse email.");
    }

    @GetMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return new MessageResponse("Adresse email verifiee.");
    }

    @PostMapping("/accept-invitation")
    public MessageResponse acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        authService.acceptInvitation(request.token(), request.password());
        return new MessageResponse("Invitation acceptee. Vous pouvez vous connecter.");
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        return authService.me();
    }
}
