package com.pfe.itsm.inventory.controller;

import com.pfe.itsm.inventory.dto.CreateSparePartRequest;
import com.pfe.itsm.inventory.dto.CreateStockMovementRequest;
import com.pfe.itsm.inventory.dto.SparePartResponse;
import com.pfe.itsm.inventory.dto.StockMovementResponse;
import com.pfe.itsm.inventory.dto.UpdateSparePartRequest;
import com.pfe.itsm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/spare-parts")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public List<SparePartResponse> listParts() {
        return inventoryService.listParts();
    }

    @PostMapping("/spare-parts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SparePartResponse createPart(@Valid @RequestBody CreateSparePartRequest request) {
        return inventoryService.createPart(request);
    }

    @GetMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public SparePartResponse getPart(@PathVariable UUID id) {
        return inventoryService.getPart(id);
    }

    @PatchMapping("/spare-parts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SparePartResponse updatePart(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSparePartRequest request
    ) {
        return inventoryService.updatePart(id, request);
    }

    @PostMapping("/spare-parts/{id}/stock-movements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TECH_N3', 'ADMIN')")
    public StockMovementResponse createMovement(
            @PathVariable UUID id,
            @Valid @RequestBody CreateStockMovementRequest request
    ) {
        return inventoryService.createMovement(id, request);
    }

    @GetMapping("/spare-parts/{id}/stock-movements")
    @PreAuthorize("hasAnyRole('TECH_N3', 'ADMIN')")
    public List<StockMovementResponse> listMovements(@PathVariable UUID id) {
        return inventoryService.listMovements(id);
    }

    @GetMapping("/stock-alerts")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3', 'ADMIN')")
    public List<SparePartResponse> lowStockAlerts() {
        return inventoryService.lowStockAlerts();
    }
}

