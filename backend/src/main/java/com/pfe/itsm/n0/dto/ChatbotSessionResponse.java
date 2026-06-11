package com.pfe.itsm.n0.dto;

import com.pfe.itsm.n0.domain.ChatbotSession;
import com.pfe.itsm.n0.domain.ChatbotSessionStatus;
import com.pfe.itsm.tickets.domain.TicketCategory;
import java.time.Instant;
import java.util.UUID;

public record ChatbotSessionResponse(
        UUID id,
        ChatbotSessionStatus statut,
        TicketCategory categorieDetectee,
        UUID ticketId,
        Instant dateCreation,
        Instant dateFermeture
) {

    public static ChatbotSessionResponse from(ChatbotSession session) {
        return new ChatbotSessionResponse(
                session.getId(),
                session.getStatut(),
                session.getCategorieDetectee(),
                session.getTicketId(),
                session.getDateCreation(),
                session.getDateFermeture()
        );
    }
}
