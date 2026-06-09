package com.pfe.itsm.inventory.dto;

import com.pfe.itsm.inventory.domain.StockMovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStockMovementRequest(
        @NotNull StockMovementType type,
        @NotNull Integer quantite,
        UUID interventionId,
        @NotBlank @Size(max = 1000) String commentaire
) {
}
