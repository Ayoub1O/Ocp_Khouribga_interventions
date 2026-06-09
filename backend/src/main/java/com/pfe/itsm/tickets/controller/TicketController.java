package com.pfe.itsm.tickets.controller;

import com.pfe.itsm.tickets.dto.CreateTicketRequest;
import com.pfe.itsm.tickets.dto.EscalateTicketRequest;
import com.pfe.itsm.tickets.dto.TicketEventResponse;
import com.pfe.itsm.tickets.dto.TicketResponse;
import com.pfe.itsm.tickets.service.TicketService;
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
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DEMANDEUR', 'ADMIN')")
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @PostMapping("/{ticketId}/claim")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public TicketResponse claim(@PathVariable UUID ticketId) {
        return ticketService.claim(ticketId);
    }

    @PostMapping("/{ticketId}/escalate")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public TicketResponse escalate(
            @PathVariable UUID ticketId,
            @Valid @RequestBody EscalateTicketRequest request
    ) {
        return ticketService.escalate(ticketId, request.raison());
    }

    @PostMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public TicketResponse resolve(@PathVariable UUID ticketId) {
        return ticketService.resolve(ticketId);
    }

    @PostMapping("/{ticketId}/close")
    @PreAuthorize("hasAnyRole('DEMANDEUR', 'ADMIN')")
    public TicketResponse close(@PathVariable UUID ticketId) {
        return ticketService.close(ticketId);
    }

    @GetMapping("/{ticketId}/events")
    public List<TicketEventResponse> events(@PathVariable UUID ticketId) {
        return ticketService.events(ticketId);
    }
}
