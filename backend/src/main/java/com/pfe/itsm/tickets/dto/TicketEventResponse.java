package com.pfe.itsm.tickets.dto;

import com.pfe.itsm.tickets.domain.TicketEvent;
import com.pfe.itsm.tickets.domain.TicketEventType;
import java.time.Instant;
import java.util.UUID;

public record TicketEventResponse(
        UUID id,
        UUID ticketId,
        UUID acteurId,
        TicketEventType type,
        String commentaire,
        Instant dateEvenement
) {

    public static TicketEventResponse from(TicketEvent event) {
        UUID acteurId = event.getActeur() == null ? null : event.getActeur().getId();
        return new TicketEventResponse(
                event.getId(),
                event.getTicket().getId(),
                acteurId,
                event.getType(),
                event.getCommentaire(),
                event.getDateEvenement()
        );
    }
}

