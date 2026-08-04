package com.pfe.itsm.users.controller;

import com.pfe.itsm.users.dto.CreateUserRequest;
import com.pfe.itsm.users.dto.InviteUserRequest;
import com.pfe.itsm.users.dto.PendingInvitationResponse;
import com.pfe.itsm.users.dto.UpdateProfileRequest;
import com.pfe.itsm.users.dto.UserResponse;
import com.pfe.itsm.users.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse invite(@Valid @RequestBody InviteUserRequest request) {
        return userService.invite(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/invitations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PendingInvitationResponse> pendingInvitations() {
        return userService.pendingTechnicianInvitations();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse get(@PathVariable UUID id) {
        return userService.get(id);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateCurrentProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateCurrentProfile(request);
    }
}
