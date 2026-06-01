package com.pfe.itsm.tickets.controller;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.dto.TicketResponse;
import com.pfe.itsm.tickets.service.TicketService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final TicketService ticketService;

    public QueueController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{level}/tickets")
    public List<TicketResponse> listQueue(@PathVariable SupportLevel level) {
        return ticketService.listQueue(level);
    }
}

