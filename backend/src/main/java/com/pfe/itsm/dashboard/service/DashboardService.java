package com.pfe.itsm.dashboard.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.dashboard.dto.AdminDashboardResponse;
import com.pfe.itsm.dashboard.dto.CountByLabelResponse;
import com.pfe.itsm.dashboard.dto.DailyTicketVolumeResponse;
import com.pfe.itsm.dashboard.dto.RequesterDashboardResponse;
import com.pfe.itsm.dashboard.dto.TechnicianDashboardResponse;
import com.pfe.itsm.interventions.domain.InterventionStatus;
import com.pfe.itsm.interventions.repository.InterventionRepository;
import com.pfe.itsm.inventory.repository.SparePartRepository;
import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.TicketStatus;
import com.pfe.itsm.tickets.repository.TicketRepository;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.sql.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<TicketStatus> ACTIVE_QUEUE_STATUSES = List.of(
            TicketStatus.OUVERT,
            TicketStatus.ESCALADE
    );

    private final TicketRepository ticketRepository;
    private final InterventionRepository interventionRepository;
    private final SparePartRepository sparePartRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(
            TicketRepository ticketRepository,
            InterventionRepository interventionRepository,
            SparePartRepository sparePartRepository,
            CurrentUserService currentUserService
    ) {
        this.ticketRepository = ticketRepository;
        this.interventionRepository = interventionRepository;
        this.sparePartRepository = sparePartRepository;
        this.currentUserService = currentUserService;
    }

    public AdminDashboardResponse adminDashboard() {
        return new AdminDashboardResponse(
                ticketRepository.count(),
                ticketRepository.countByStatut(TicketStatus.OUVERT),
                ticketRepository.countByStatut(TicketStatus.RESOLU),
                interventionRepository.count(),
                sparePartRepository.countLowStockParts(),
                toCounts(ticketRepository.countGroupedByStatut()),
                toCounts(ticketRepository.countGroupedByNiveauCourant()),
                toCounts(interventionRepository.countGroupedByStatut()),
                toDailyVolume(ticketRepository.countDailyVolumeLastSevenDays())
        );
    }

    public TechnicianDashboardResponse technicianDashboard() {
        UserAccount technicien = currentUserService.currentUser();
        UUID technicienId = technicien.getId();
        SupportLevel niveau = supportLevelForRole(technicien.getRole());

        return new TechnicianDashboardResponse(
                ticketRepository.countByTechnicienAssigneId(technicienId),
                ticketRepository.countByTechnicienAssigneIdAndStatut(technicienId, TicketStatus.EN_COURS),
                ticketRepository.countByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
                        niveau,
                        ACTIVE_QUEUE_STATUSES
                ),
                interventionRepository.countByTechnicienIdAndStatut(technicienId, InterventionStatus.PLANIFIEE),
                sparePartRepository.countLowStockParts(),
                toCounts(ticketRepository.countGroupedByStatutForTechnicien(technicienId)),
                toCounts(interventionRepository.countGroupedByStatutForTechnicien(technicienId))
        );
    }

    public RequesterDashboardResponse requesterDashboard() {
        UUID demandeurId = currentUserService.currentUserId();
        return new RequesterDashboardResponse(
                ticketRepository.countByDemandeurId(demandeurId),
                ticketRepository.countByDemandeurIdAndStatut(demandeurId, TicketStatus.OUVERT),
                ticketRepository.countByDemandeurIdAndStatut(demandeurId, TicketStatus.RESOLU),
                toCounts(ticketRepository.countGroupedByStatutForDemandeur(demandeurId)),
                toDailyVolume(ticketRepository.countDailyVolumeLastSevenDaysForDemandeur(demandeurId))
        );
    }

    private SupportLevel supportLevelForRole(UserRole role) {
        return switch (role) {
            case TECH_N1 -> SupportLevel.N1;
            case TECH_N2 -> SupportLevel.N2;
            case TECH_N3 -> SupportLevel.N3;
            default -> throw new IllegalStateException("Role technicien attendu pour ce tableau de bord.");
        };
    }

    private List<CountByLabelResponse> toCounts(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new CountByLabelResponse(String.valueOf(row[0]), (Long) row[1]))
                .toList();
    }

    private List<DailyTicketVolumeResponse> toDailyVolume(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new DailyTicketVolumeResponse(toLocalDate(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        throw new IllegalStateException("Type date inattendu dans le volume quotidien: " + value.getClass().getName());
    }
}
