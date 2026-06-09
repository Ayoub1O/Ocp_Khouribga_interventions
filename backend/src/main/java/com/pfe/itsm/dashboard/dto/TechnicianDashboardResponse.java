package com.pfe.itsm.dashboard.dto;

import java.util.List;

public record TechnicianDashboardResponse(
        long ticketsAssignes,
        long ticketsEnCours,
        long ticketsFileNiveau,
        long interventionsPlanifiees,
        long piecesEnAlerte,
        List<CountByLabelResponse> ticketsAssignesParStatut,
        List<CountByLabelResponse> interventionsParStatut
) {
}
