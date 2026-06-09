package com.pfe.itsm.inventory.dto;

import com.pfe.itsm.inventory.domain.StockMovement;
import com.pfe.itsm.inventory.domain.StockMovementType;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID pieceId,
        StockMovementType type,
        int quantite,
        UUID interventionId,
        UUID technicienId,
        String commentaire,
        Instant dateMouvement
) {

    public static StockMovementResponse from(StockMovement movement) {
        UUID interventionId = movement.getIntervention() == null ? null : movement.getIntervention().getId();
        return new StockMovementResponse(
                movement.getId(),
                movement.getPiece().getId(),
                movement.getType(),
                movement.getQuantite(),
                interventionId,
                movement.getTechnicien().getId(),
                movement.getCommentaire(),
                movement.getDateMouvement()
        );
    }
}

