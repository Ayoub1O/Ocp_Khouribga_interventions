package com.pfe.itsm.interventions.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.interventions.domain.Intervention;
import com.pfe.itsm.interventions.dto.CancelInterventionRequest;
import com.pfe.itsm.interventions.dto.CompleteInterventionRequest;
import com.pfe.itsm.interventions.dto.CreateInterventionRequest;
import com.pfe.itsm.interventions.dto.InterventionResponse;
import com.pfe.itsm.interventions.repository.InterventionRepository;
import com.pfe.itsm.notifications.domain.NotificationType;
import com.pfe.itsm.notifications.service.NotificationService;
import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketEvent;
import com.pfe.itsm.tickets.domain.TicketEventType;
import com.pfe.itsm.tickets.repository.TicketEventRepository;
import com.pfe.itsm.tickets.repository.TicketRepository;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public InterventionService(
            InterventionRepository interventionRepository,
            TicketRepository ticketRepository,
            TicketEventRepository ticketEventRepository,
            UserAccountRepository userAccountRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.interventionRepository = interventionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.userAccountRepository = userAccountRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public InterventionResponse create(CreateInterventionRequest request) {
        UserAccount actor = currentUserService.currentUser();
        requireInterventionPlanner(actor);

        if (!request.dateFinPrevue().isAfter(request.dateDebutPrevue())) {
            throw new BusinessException("La date de fin prevue doit etre apres la date de debut prevue.");
        }

        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));
        UserAccount technicien = userAccountRepository.findById(request.technicienId())
                .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable."));
        requireN2OrN3Technician(technicien);

        Intervention intervention = interventionRepository.save(new Intervention(
                ticket,
                technicien,
                request.dateDebutPrevue(),
                request.dateFinPrevue(),
                request.lieu().trim()
        ));
        addTicketEvent(ticket, actor, TicketEventType.INTERVENTION_PLANIFIEE, "Intervention planifiee.");
        InterventionResponse response = InterventionResponse.from(intervention);
        notificationService.notifyUser(
                technicien,
                NotificationType.INTERVENTION_PLANIFIEE,
                "Nouvelle intervention",
                "Une intervention vous a ete planifiee pour le ticket " + ticket.getReference() + ".",
                "INTERVENTION",
                intervention.getId()
        );
        notificationService.notifyUser(
                ticket.getDemandeur(),
                NotificationType.INTERVENTION_PLANIFIEE,
                "Intervention planifiee",
                "Une intervention a ete planifiee pour votre ticket " + ticket.getReference() + ".",
                "INTERVENTION",
                intervention.getId()
        );
        notificationService.publishTicketUpdate(ticket.getId(), response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<InterventionResponse> list() {
        UserAccount user = currentUserService.currentUser();
        if (user.getRole() == UserRole.ADMIN) {
            return interventionRepository.findAll().stream().map(InterventionResponse::from).toList();
        }
        if (user.getRole() == UserRole.TECH_N2 || user.getRole() == UserRole.TECH_N3) {
            return interventionRepository.findByTechnicienIdOrderByDateDebutPrevueAsc(user.getId())
                    .stream()
                    .map(InterventionResponse::from)
                    .toList();
        }
        throw new BusinessException("Acces aux interventions non autorise.");
    }

    @Transactional(readOnly = true)
    public InterventionResponse get(UUID interventionId) {
        Intervention intervention = findIntervention(interventionId);
        requireVisibility(intervention, currentUserService.currentUser());
        return InterventionResponse.from(intervention);
    }

    @Transactional(readOnly = true)
    public List<InterventionResponse> listByTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));
        requireTicketInterventionVisibility(ticket, currentUserService.currentUser());

        return interventionRepository.findByTicketIdOrderByDateDebutPrevueAsc(ticketId)
                .stream()
                .map(InterventionResponse::from)
                .toList();
    }

    @Transactional
    public InterventionResponse start(UUID interventionId) {
        Intervention intervention = findIntervention(interventionId);
        UserAccount actor = currentUserService.currentUser();
        requireAssignedTechnicianOrAdmin(intervention, actor);

        intervention.start();
        addTicketEvent(intervention.getTicket(), actor, TicketEventType.INTERVENTION_DEMARREE, "Intervention demarree.");
        return InterventionResponse.from(intervention);
    }

    @Transactional
    public InterventionResponse complete(UUID interventionId, CompleteInterventionRequest request) {
        Intervention intervention = findIntervention(interventionId);
        UserAccount actor = currentUserService.currentUser();
        requireAssignedTechnicianOrAdmin(intervention, actor);

        intervention.complete(request.rapport().trim());
        addTicketEvent(intervention.getTicket(), actor, TicketEventType.INTERVENTION_TERMINEE, request.rapport().trim());
        InterventionResponse response = InterventionResponse.from(intervention);
        notificationService.notifyUser(
                intervention.getTicket().getDemandeur(),
                NotificationType.INTERVENTION_TERMINEE,
                "Intervention terminee",
                "Une intervention liee a votre ticket est terminee.",
                "INTERVENTION",
                intervention.getId()
        );
        notificationService.publishTicketUpdate(intervention.getTicket().getId(), response);
        return response;
    }

    @Transactional
    public InterventionResponse cancel(UUID interventionId, CancelInterventionRequest request) {
        Intervention intervention = findIntervention(interventionId);
        UserAccount actor = currentUserService.currentUser();
        requireInterventionPlanner(actor);

        intervention.cancel(request.raison().trim());
        addTicketEvent(intervention.getTicket(), actor, TicketEventType.INTERVENTION_ANNULEE, request.raison().trim());
        return InterventionResponse.from(intervention);
    }

    private Intervention findIntervention(UUID interventionId) {
        return interventionRepository.findById(interventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention introuvable."));
    }

    private void addTicketEvent(Ticket ticket, UserAccount actor, TicketEventType type, String commentaire) {
        ticketEventRepository.save(new TicketEvent(ticket, actor, type, commentaire));
    }

    private void requireInterventionPlanner(UserAccount user) {
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.TECH_N2 && user.getRole() != UserRole.TECH_N3) {
            throw new BusinessException("Seul N2, N3 ou un administrateur peut planifier une intervention.");
        }
    }

    private void requireN2OrN3Technician(UserAccount user) {
        if (user.getRole() != UserRole.TECH_N2 && user.getRole() != UserRole.TECH_N3) {
            throw new BusinessException("L'intervention doit etre assignee a un technicien N2 ou N3.");
        }
    }

    private void requireAssignedTechnicianOrAdmin(Intervention intervention, UserAccount user) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!intervention.getTechnicien().getId().equals(user.getId())) {
            throw new BusinessException("Seul le technicien assigne peut modifier cette intervention.");
        }
    }

    private void requireVisibility(Intervention intervention, UserAccount user) {
        if (user.getRole() == UserRole.ADMIN || intervention.getTechnicien().getId().equals(user.getId())) {
            return;
        }
        requireTicketInterventionVisibility(intervention.getTicket(), user);
    }

    private void requireTicketInterventionVisibility(Ticket ticket, UserAccount user) {
        if (user.getRole() == UserRole.ADMIN || ticket.getDemandeur().getId().equals(user.getId())) {
            return;
        }
        if (ticket.getTechnicienAssigne() != null && ticket.getTechnicienAssigne().getId().equals(user.getId())) {
            return;
        }
        if (ticket.getNiveauCourant() == SupportLevel.N2 && user.getRole() == UserRole.TECH_N2) {
            return;
        }
        if (ticket.getNiveauCourant() == SupportLevel.N3 && user.getRole() == UserRole.TECH_N3) {
            return;
        }
        throw new BusinessException("Acces aux interventions du ticket non autorise.");
    }
}

