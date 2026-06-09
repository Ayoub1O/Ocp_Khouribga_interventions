package com.pfe.itsm.inventory.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.interventions.domain.Intervention;
import com.pfe.itsm.interventions.repository.InterventionRepository;
import com.pfe.itsm.inventory.domain.SparePart;
import com.pfe.itsm.inventory.domain.StockMovement;
import com.pfe.itsm.inventory.domain.StockMovementType;
import com.pfe.itsm.inventory.dto.CreateSparePartRequest;
import com.pfe.itsm.inventory.dto.CreateStockMovementRequest;
import com.pfe.itsm.inventory.dto.SparePartResponse;
import com.pfe.itsm.inventory.dto.StockMovementResponse;
import com.pfe.itsm.inventory.dto.UpdateSparePartRequest;
import com.pfe.itsm.inventory.repository.SparePartRepository;
import com.pfe.itsm.inventory.repository.StockMovementRepository;
import com.pfe.itsm.notifications.domain.NotificationType;
import com.pfe.itsm.notifications.service.NotificationService;
import com.pfe.itsm.tickets.domain.TicketEvent;
import com.pfe.itsm.tickets.domain.TicketEventType;
import com.pfe.itsm.tickets.repository.TicketEventRepository;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final SparePartRepository sparePartRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InterventionRepository interventionRepository;
    private final TicketEventRepository ticketEventRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public InventoryService(
            SparePartRepository sparePartRepository,
            StockMovementRepository stockMovementRepository,
            InterventionRepository interventionRepository,
            TicketEventRepository ticketEventRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService
    ) {
        this.sparePartRepository = sparePartRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.interventionRepository = interventionRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public SparePartResponse createPart(CreateSparePartRequest request) {
        requireAdmin(currentUserService.currentUser());
        String reference = normalizeReference(request.reference());
        if (sparePartRepository.existsByReference(reference)) {
            throw new BusinessException("Une piece existe deja avec cette reference.");
        }

        SparePart part = sparePartRepository.save(new SparePart(
                reference,
                request.nom().trim(),
                trimNullable(request.description()),
                request.quantiteInitiale(),
                request.seuilAlerte()
        ));
        if (request.quantiteInitiale() > 0) {
            recordMovement(
                    part,
                    StockMovementType.ENTREE,
                    request.quantiteInitiale(),
                    null,
                    currentUserService.currentUser(),
                    "Stock initial."
            );
        }
        return SparePartResponse.from(part);
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> listParts() {
        return sparePartRepository.findAll().stream().map(SparePartResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SparePartResponse getPart(UUID partId) {
        return SparePartResponse.from(findPart(partId));
    }

    @Transactional
    public SparePartResponse updatePart(UUID partId, UpdateSparePartRequest request) {
        requireAdmin(currentUserService.currentUser());
        SparePart part = findPart(partId);
        part.update(
                request.nom().trim(),
                trimNullable(request.description()),
                request.seuilAlerte(),
                request.actif()
        );
        return SparePartResponse.from(part);
    }

    @Transactional
    public StockMovementResponse createMovement(UUID partId, CreateStockMovementRequest request) {
        UserAccount actor = currentUserService.currentUser();
        SparePart part = findPart(partId);
        Intervention intervention = request.interventionId() == null ? null : findIntervention(request.interventionId());
        validateMovementQuantity(request);

        switch (request.type()) {
            case ENTREE -> {
                requireAdmin(actor);
                part.addStock(request.quantite());
            }
            case AJUSTEMENT -> {
                requireAdmin(actor);
                part.adjustStock(request.quantite());
            }
            case SORTIE -> {
                requireN3OrAdmin(actor);
                if (intervention == null) {
                    throw new BusinessException("Une sortie de stock doit etre liee a une intervention.");
                }
                part.consumeStock(request.quantite());
            }
        }

        StockMovement movement = recordMovement(
                part,
                request.type(),
                request.quantite(),
                intervention,
                actor,
                request.commentaire().trim()
        );
        if (request.type() == StockMovementType.SORTIE && intervention != null) {
            ticketEventRepository.save(new TicketEvent(
                    intervention.getTicket(),
                    actor,
                    TicketEventType.PIECE_CONSOMMEE,
                    "Piece consommee: " + part.getReference() + " x" + request.quantite()
            ));
            notificationService.publishTicketUpdate(intervention.getTicket().getId(), StockMovementResponse.from(movement));
        }
        if (part.isLowStock()) {
            notificationService.notifyRole(
                    UserRole.ADMIN,
                    NotificationType.STOCK_BAS,
                    "Alerte stock bas",
                    "La piece " + part.getReference() + " a atteint son seuil d'alerte.",
                    "SPARE_PART",
                    part.getId()
            );
        }
        return StockMovementResponse.from(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovements(UUID partId) {
        findPart(partId);
        return stockMovementRepository.findByPieceIdOrderByDateMouvementDesc(partId)
                .stream()
                .map(StockMovementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> lowStockAlerts() {
        return sparePartRepository.findLowStockParts()
                .stream()
                .map(SparePartResponse::from)
                .toList();
    }

    private StockMovement recordMovement(
            SparePart part,
            StockMovementType type,
            int quantity,
            Intervention intervention,
            UserAccount actor,
            String commentaire
    ) {
        return stockMovementRepository.save(new StockMovement(part, type, quantity, intervention, actor, commentaire));
    }

    private SparePart findPart(UUID partId) {
        return sparePartRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Piece introuvable."));
    }

    private Intervention findIntervention(UUID interventionId) {
        return interventionRepository.findById(interventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention introuvable."));
    }

    private void validateMovementQuantity(CreateStockMovementRequest request) {
        if (request.type() == StockMovementType.AJUSTEMENT) {
            if (request.quantite() < 0) {
                throw new BusinessException("La quantite ajustee ne peut pas etre negative.");
            }
            return;
        }
        if (request.quantite() <= 0) {
            throw new BusinessException("La quantite doit etre positive.");
        }
    }

    private void requireAdmin(UserAccount user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Action reservee a l'administrateur.");
        }
    }

    private void requireN3OrAdmin(UserAccount user) {
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.TECH_N3) {
            throw new BusinessException("Seul N3 ou un administrateur peut consommer une piece.");
        }
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase(Locale.ROOT);
    }

    private String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
