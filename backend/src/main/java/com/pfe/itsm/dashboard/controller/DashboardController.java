package com.pfe.itsm.dashboard.controller;

import com.pfe.itsm.dashboard.dto.AdminDashboardResponse;
import com.pfe.itsm.dashboard.dto.RequesterDashboardResponse;
import com.pfe.itsm.dashboard.dto.TechnicianDashboardResponse;
import com.pfe.itsm.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse adminDashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/technician")
    @PreAuthorize("hasAnyRole('TECH_N1', 'TECH_N2', 'TECH_N3')")
    public TechnicianDashboardResponse technicianDashboard() {
        return dashboardService.technicianDashboard();
    }

    @GetMapping("/requester")
    @PreAuthorize("hasRole('DEMANDEUR')")
    public RequesterDashboardResponse requesterDashboard() {
        return dashboardService.requesterDashboard();
    }
}
