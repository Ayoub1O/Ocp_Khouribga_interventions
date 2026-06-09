package com.pfe.itsm.inventory.dto;

import com.pfe.itsm.inventory.domain.SparePart;
import java.time.Instant;
import java.util.UUID;

public record SparePartResponse(
        UUID id,
        String reference,
        String nom,
        String description,
        int quantiteDisponible,
        int seuilAlerte,
        boolean actif,
        boolean lowStock,
        Instant dateCreation
) {

    public static SparePartResponse from(SparePart part) {
        return new SparePartResponse(
                part.getId(),
                part.getReference(),
                part.getNom(),
                part.getDescription(),
                part.getQuantiteDisponible(),
                part.getSeuilAlerte(),
                part.isActif(),
                part.isLowStock(),
                part.getDateCreation()
        );
    }
}

