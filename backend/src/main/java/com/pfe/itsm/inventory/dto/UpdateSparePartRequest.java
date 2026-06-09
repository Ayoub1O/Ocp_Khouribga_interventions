package com.pfe.itsm.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSparePartRequest(
        @NotBlank @Size(max = 180) String nom,
        @Size(max = 1000) String description,
        @NotNull @Min(0) Integer seuilAlerte,
        @NotNull Boolean actif
) {
}

