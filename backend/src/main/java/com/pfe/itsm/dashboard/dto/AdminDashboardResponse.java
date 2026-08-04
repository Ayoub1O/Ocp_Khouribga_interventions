package com.pfe.itsm.dashboard.dto;

import java.util.List;

public record AdminDashboardResponse(
        long totalTickets,
        long ticketsOuverts,
        long ticketsResolus,
        long totalInterventions,
        long piecesEnAlerte,
        List<CountByLabelResponse> ticketsParStatut,
        List<CountByLabelResponse> ticketsParNiveau,
        List<CountByLabelResponse> interventionsParStatut,
        List<DailyTicketVolumeResponse> volumeTicketsParJour
) {
}
