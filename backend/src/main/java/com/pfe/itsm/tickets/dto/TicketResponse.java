package com.pfe.itsm.tickets.dto;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;
import com.pfe.itsm.tickets.domain.TicketStatus;
import java.time.Instant;
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
        String demandeurNomComplet,
        String demandeurTelephone,
        UUID technicienAssigneId,
        String technicienAssigneNomComplet,
        Instant dateCreation,
        Instant dateDerniereModification,
        Instant dateResolution,
        Instant dateCloture,
        String codeAchevement,
        String commentaireResolution,
        String feedbackCloture
) {

    public static TicketResponse from(Ticket ticket) {
        return from(ticket, ticket.getCommentaireResolution(), ticket.getFeedbackCloture());
    }

    public static TicketResponse from(Ticket ticket, String commentaireResolution, String feedbackCloture) {
        UUID technicienId = ticket.getTechnicienAssigne() == null ? null : ticket.getTechnicienAssigne().getId();
        String technicienNom = ticket.getTechnicienAssigne() == null
                ? null
                : ticket.getTechnicienAssigne().getPrenom() + " " + ticket.getTechnicienAssigne().getNom();
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
                ticket.getDemandeur().getPrenom() + " " + ticket.getDemandeur().getNom(),
                ticket.getDemandeur().getTelephone(),
                technicienId,
                technicienNom,
                ticket.getDateCreation(),
                ticket.getDateDerniereModification(),
                ticket.getDateResolution(),
                ticket.getDateCloture(),
                commentaireResolution,
                commentaireResolution,
                feedbackCloture
        );
    }
}
