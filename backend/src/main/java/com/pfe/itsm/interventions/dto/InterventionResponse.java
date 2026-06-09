package com.pfe.itsm.interventions.dto;

import com.pfe.itsm.interventions.domain.Intervention;
import com.pfe.itsm.interventions.domain.InterventionStatus;
import java.time.Instant;
import java.util.UUID;

public record InterventionResponse(
        UUID id,
        UUID ticketId,
        UUID technicienId,
        InterventionStatus statut,
        Instant dateDebutPrevue,
        Instant dateFinPrevue,
        Instant dateDebutReelle,
        Instant dateFinReelle,
        String lieu,
        String rapport,
        Instant dateCreation
) {

    public static InterventionResponse from(Intervention intervention) {
        return new InterventionResponse(
                intervention.getId(),
                intervention.getTicket().getId(),
                intervention.getTechnicien().getId(),
                intervention.getStatut(),
                intervention.getDateDebutPrevue(),
                intervention.getDateFinPrevue(),
                intervention.getDateDebutReelle(),
                intervention.getDateFinReelle(),
                intervention.getLieu(),
                intervention.getRapport(),
                intervention.getDateCreation()
        );
    }
}

