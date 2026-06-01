package com.pfe.itsm.tickets.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
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

    public TicketService(
            TicketRepository ticketRepository,
            TicketEventRepository ticketEventRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        UserAccount demandeur = findUser(request.demandeurId());
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
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listQueue(SupportLevel level) {
        return ticketRepository.findByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
                        level,
                        List.of(TicketStatus.OUVERT, TicketStatus.ESCALADE)
                )
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional
    public TicketResponse claim(UUID ticketId, UUID technicienId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount technicien = findUser(technicienId);
        requireTechnicianForLevel(technicien, ticket.getNiveauCourant());

        ticket.claim(technicien);
        addEvent(ticket, technicien, TicketEventType.PRIS_EN_CHARGE, "Ticket pris en charge.");
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse escalate(UUID ticketId, UUID acteurId, String raison) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = acteurId == null ? null : findUser(acteurId);
        SupportLevel nextLevel = nextLevel(ticket.getNiveauCourant());

        ticket.escalate(nextLevel);
        addEvent(ticket, acteur, eventTypeForEscalation(nextLevel), raison);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse resolve(UUID ticketId, UUID acteurId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = acteurId == null ? null : findUser(acteurId);

        ticket.resolve();
        addEvent(ticket, acteur, TicketEventType.RESOLU, "Ticket resolu.");
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse close(UUID ticketId, UUID acteurId) {
        Ticket ticket = findLockedTicket(ticketId);
        UserAccount acteur = acteurId == null ? null : findUser(acteurId);

        ticket.close();
        addEvent(ticket, acteur, TicketEventType.CLOTURE, "Ticket cloture.");
        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketEventResponse> events(UUID ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket introuvable.");
        }
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
}

