package com.pfe.itsm.tickets.controller;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.dto.CreateTicketRequest;
import com.pfe.itsm.tickets.dto.EscalateTicketRequest;
import com.pfe.itsm.tickets.dto.TicketEventResponse;
import com.pfe.itsm.tickets.dto.TicketResponse;
import com.pfe.itsm.tickets.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @PostMapping("/{ticketId}/claim")
    public TicketResponse claim(
            @PathVariable UUID ticketId,
            @RequestHeader("X-User-Id") UUID technicienId
    ) {
        return ticketService.claim(ticketId, technicienId);
    }

    @PostMapping("/{ticketId}/escalate")
    public TicketResponse escalate(
            @PathVariable UUID ticketId,
            @Valid @RequestBody EscalateTicketRequest request
    ) {
        return ticketService.escalate(ticketId, request.acteurId(), request.raison());
    }

    @PostMapping("/{ticketId}/resolve")
    public TicketResponse resolve(
            @PathVariable UUID ticketId,
            @RequestHeader(value = "X-User-Id", required = false) UUID acteurId
    ) {
        return ticketService.resolve(ticketId, acteurId);
    }

    @PostMapping("/{ticketId}/close")
    public TicketResponse close(
            @PathVariable UUID ticketId,
            @RequestHeader(value = "X-User-Id", required = false) UUID acteurId
    ) {
        return ticketService.close(ticketId, acteurId);
    }

    @GetMapping("/{ticketId}/events")
    public List<TicketEventResponse> events(@PathVariable UUID ticketId) {
        return ticketService.events(ticketId);
    }
}

