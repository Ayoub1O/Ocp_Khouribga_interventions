package com.pfe.itsm.n0.ai;

import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;

public record N0TicketDraft(
        String titre,
        String description,
        TicketCategory categorie,
        TicketPriority priorite
) {
}
