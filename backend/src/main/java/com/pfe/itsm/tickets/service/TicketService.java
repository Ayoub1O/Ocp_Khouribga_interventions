package com.pfe.itsm.tickets.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.notifications.domain.NotificationType;
import com.pfe.itsm.notifications.service.NotificationService;
import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketEvent;
import com.pfe.itsm.tickets.domain.TicketEventType;
import com.pfe.itsm.tickets.domain.TicketStatus;
import com.pfe.itsm.tickets.dto.CreateTicketRequest;
import com.pfe.itsm.tickets.dto.TicketEventResponse;
import com.pfe.itsm.tickets.dto.TicketResponse;
import com.pfe.itsm.tickets.repository.TicketEventRepository;
import com.pfe.itsm.tickets.repository.TicketRepository;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public TicketService(
            TicketRepository ticketRepository,
            TicketEventRepository ticketEventRepository,
            UserAccountRepository userAccountRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.userAccountRepository = userAccountRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        UserAccount demandeur = currentUserService.currentUser();
        if (demandeur.getRole() != UserRole.DEMANDEUR && demandeur.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Seul un demandeur peut declarer un ticket.");
        }

        Ticket ticket = new Ticket(
                nextReference(),
                request.titre(),
                request.description(),
                request.categorie(),
                request.priorite(),
                demandeur
        );

        Ticket saved = ticketRepository.save(ticket);
        addEvent(saved, demandeur, TicketEventType.TICKET_CREE, "Ticket cree par le demandeur.");
        notificationService.notifyRole(
                UserRole.TECH_N1,
                NotificationType.TICKET_CREE,
                "Nouveau ticket N1",
                "Un nouveau ticket est disponible dans la file N1: " + saved.getReference(),
                "TICKET",
                saved.getId()
        );
        notificationService.publishQueueUpdate(saved.getNiveauCourant(), TicketResponse.from(saved));
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listQueue(SupportLevel level) {
        UserAccount user = currentUserService.currentUser();
        requireQueueAccess(user, level);

        return ticketRepository.findByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
                        level,
                        List.of(TicketStatus.OUVERT, TicketStatus.ESCALADE)
                )
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional
    public TicketResponse claim(UUID ticketId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount technicien = currentUserService.currentUser();
        requireTechnicianForLevel(technicien, ticket.getNiveauCourant());

        ticket.claim(technicien);
        addEvent(ticket, technicien, TicketEventType.PRIS_EN_CHARGE, "Ticket pris en charge.");
        TicketResponse response = TicketResponse.from(ticket);
        notificationService.notifyUser(
                ticket.getDemandeur(),
                NotificationType.TICKET_PRIS_EN_CHARGE,
                "Ticket pris en charge",
                "Votre ticket " + ticket.getReference() + " est pris en charge.",
                "TICKET",
                ticket.getId()
        );
        notificationService.publishTicketUpdate(ticket.getId(), response);
        notificationService.publishQueueUpdate(ticket.getNiveauCourant(), response);
        return response;
    }

    @Transactional
    public TicketResponse escalate(UUID ticketId, String raison) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = currentUserService.currentUser();
        requireTicketActor(ticket, acteur);
        SupportLevel nextLevel = nextLevel(ticket.getNiveauCourant());

        ticket.escalate(nextLevel);
        addEvent(ticket, acteur, eventTypeForEscalation(nextLevel), raison);
        TicketResponse response = TicketResponse.from(ticket);
        notificationService.notifyUser(
                ticket.getDemandeur(),
                NotificationType.TICKET_ESCALADE,
                "Ticket escalade",
                "Votre ticket " + ticket.getReference() + " a ete escalade vers " + nextLevel + ".",
                "TICKET",
                ticket.getId()
        );
        notifyQueueForLevel(nextLevel, ticket);
        notificationService.publishTicketUpdate(ticket.getId(), response);
        notificationService.publishQueueUpdate(nextLevel, response);
        return response;
    }

    @Transactional
    public TicketResponse resolve(UUID ticketId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = currentUserService.currentUser();
        requireTicketActor(ticket, acteur);

        ticket.resolve();
        addEvent(ticket, acteur, TicketEventType.RESOLU, "Ticket resolu.");
        TicketResponse response = TicketResponse.from(ticket);
        notificationService.notifyUser(
                ticket.getDemandeur(),
                NotificationType.TICKET_RESOLU,
                "Ticket resolu",
                "Votre ticket " + ticket.getReference() + " est marque comme resolu.",
                "TICKET",
                ticket.getId()
        );
        notificationService.publishTicketUpdate(ticket.getId(), response);
        return response;
    }

    @Transactional
    public TicketResponse close(UUID ticketId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = currentUserService.currentUser();
        if (acteur.getRole() != UserRole.ADMIN && !ticket.getDemandeur().getId().equals(acteur.getId())) {
            throw new BusinessException("Seul le demandeur ou un administrateur peut cloturer ce ticket.");
        }

        ticket.close();
        addEvent(ticket, acteur, TicketEventType.CLOTURE, "Ticket cloture.");
        TicketResponse response = TicketResponse.from(ticket);
        notificationService.publishTicketUpdate(ticket.getId(), response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<TicketEventResponse> events(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));
        requireTicketVisibility(ticket, currentUserService.currentUser());
        return ticketEventRepository.findByTicketIdOrderByDateEvenementAsc(ticketId)
                .stream()
                .map(TicketEventResponse::from)
                .toList();
    }

    private Ticket findLockedTicket(UUID ticketId) {
        return ticketRepository.findLockedById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));
    }

    private UserAccount findUser(UUID userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private void addEvent(Ticket ticket, UserAccount acteur, TicketEventType type, String commentaire) {
        ticketEventRepository.save(new TicketEvent(ticket, acteur, type, commentaire));
    }

    private String nextReference() {
        long count = ticketRepository.count() + 1;
        return "INC-" + Year.now().getValue() + "-" + String.format("%06d", count);
    }

    private SupportLevel nextLevel(SupportLevel current) {
        return switch (current) {
            case N0 -> SupportLevel.N1;
            case N1 -> SupportLevel.N2;
            case N2 -> SupportLevel.N3;
            case N3 -> throw new BusinessException("Un ticket N3 ne peut pas etre escalade davantage.");
        };
    }

    private TicketEventType eventTypeForEscalation(SupportLevel nextLevel) {
        return switch (nextLevel) {
            case N1 -> TicketEventType.ESCALADE_VERS_N1;
            case N2 -> TicketEventType.ESCALADE_VERS_N2;
            case N3 -> TicketEventType.ESCALADE_VERS_N3;
            case N0 -> throw new BusinessException("Escalade vers N0 invalide.");
        };
    }

    private void notifyQueueForLevel(SupportLevel level, Ticket ticket) {
        UserRole role = switch (level) {
            case N1 -> UserRole.TECH_N1;
            case N2 -> UserRole.TECH_N2;
            case N3 -> UserRole.TECH_N3;
            case N0 -> null;
        };
        if (role != null) {
            notificationService.notifyRole(
                    role,
                    NotificationType.TICKET_ESCALADE,
                    "Ticket disponible " + level,
                    "Le ticket " + ticket.getReference() + " est disponible dans la file " + level + ".",
                    "TICKET",
                    ticket.getId()
            );
        }
    }

    private void requireTechnicianForLevel(UserAccount technicien, SupportLevel level) {
        UserRole expectedRole = switch (level) {
            case N1 -> UserRole.TECH_N1;
            case N2 -> UserRole.TECH_N2;
            case N3 -> UserRole.TECH_N3;
            case N0 -> throw new BusinessException("Les tickets N0 ne sont pas adoptes par un technicien.");
        };

        if (technicien.getRole() != expectedRole && technicien.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Le technicien n'appartient pas au niveau de support requis.");
        }
    }

    private void requireQueueAccess(UserAccount user, SupportLevel level) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        requireTechnicianForLevel(user, level);
    }

    private void requireTicketActor(Ticket ticket, UserAccount actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (ticket.getTechnicienAssigne() == null || !ticket.getTechnicienAssigne().getId().equals(actor.getId())) {
            throw new BusinessException("Seul le technicien en charge peut modifier ce ticket.");
        }
    }

    private void requireTicketVisibility(Ticket ticket, UserAccount user) {
        if (user.getRole() == UserRole.ADMIN || ticket.getDemandeur().getId().equals(user.getId())) {
            return;
        }
        if (ticket.getTechnicienAssigne() != null && ticket.getTechnicienAssigne().getId().equals(user.getId())) {
            return;
        }
        if (isTechnicianForLevel(user, ticket.getNiveauCourant())) {
            return;
        }
        throw new BusinessException("Acces au ticket non autorise.");
    }

    private boolean isTechnicianForLevel(UserAccount user, SupportLevel level) {
        return switch (level) {
            case N0 -> false;
            case N1 -> user.getRole() == UserRole.TECH_N1;
            case N2 -> user.getRole() == UserRole.TECH_N2;
            case N3 -> user.getRole() == UserRole.TECH_N3;
        };
    }
}
