package com.pfe.itsm.tickets.dto;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;
import com.pfe.itsm.tickets.domain.TicketStatus;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String reference,
        String titre,
        String description,
        TicketCategory categorie,
        TicketPriority priorite,
        TicketStatus statut,
        SupportLevel niveauCourant,
        UUID demandeurId,
        UUID technicienAssigneId
) {

    public static TicketResponse from(Ticket ticket) {
        UUID technicienId = ticket.getTechnicienAssigne() == null ? null : ticket.getTechnicienAssigne().getId();
        return new TicketResponse(
                ticket.getId(),
                ticket.getReference(),
                ticket.getTitre(),
                ticket.getDescription(),
                ticket.getCategorie(),
                ticket.getPriorite(),
                ticket.getStatut(),
                ticket.getNiveauCourant(),
                ticket.getDemandeur().getId(),
                technicienId
        );
    }
}

