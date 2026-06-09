package com.pfe.itsm.dashboard.dto;

import java.util.List;

public record RequesterDashboardResponse(
        long totalTickets,
        long ticketsOuverts,
        long ticketsResolus,
        List<CountByLabelResponse> ticketsParStatut
) {
}
