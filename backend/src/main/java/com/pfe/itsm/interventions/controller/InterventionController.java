package com.pfe.itsm.interventions.controller;

import com.pfe.itsm.interventions.dto.CancelInterventionRequest;
import com.pfe.itsm.interventions.dto.CompleteInterventionRequest;
import com.pfe.itsm.interventions.dto.CreateInterventionRequest;
import com.pfe.itsm.interventions.dto.InterventionResponse;
import com.pfe.itsm.interventions.service.InterventionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interventions")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TECH_N2', 'TECH_N3', 'ADMIN')")
    public InterventionResponse create(@Valid @RequestBody CreateInterventionRequest request) {
        return interventionService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TECH_N2', 'TECH_N3', 'ADMIN')")
    public List<InterventionResponse> list() {
        return interventionService.list();
    }

    @GetMapping("/{id}")
    public InterventionResponse get(@PathVariable UUID id) {
        return interventionService.get(id);
    }

    @GetMapping("/ticket/{ticketId}")
    public List<InterventionResponse> listByTicket(@PathVariable UUID ticketId) {
        return interventionService.listByTicket(ticketId);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('TECH_N2', 'TECH_N3', 'ADMIN')")
    public InterventionResponse start(@PathVariable UUID id) {
        return interventionService.start(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TECH_N2', 'TECH_N3', 'ADMIN')")
    public InterventionResponse complete(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteInterventionRequest request
    ) {
        return interventionService.complete(id, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TECH_N2', 'TECH_N3', 'ADMIN')")
    public InterventionResponse cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelInterventionRequest request
    ) {
        return interventionService.cancel(id, request);
    }
}

