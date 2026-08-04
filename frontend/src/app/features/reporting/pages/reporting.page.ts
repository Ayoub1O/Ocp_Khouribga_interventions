import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/auth/auth.models';
import { DashboardService } from '../../../core/dashboard/dashboard.service';
import { CountByLabel, DailyTicketVolume } from '../../../core/dashboard/dashboard.models';
import { InterventionsService } from '../../../core/interventions/interventions.service';
import { Intervention } from '../../../core/interventions/interventions.models';
import { InventoryService } from '../../../core/inventory/inventory.service';
import { SparePart } from '../../../core/inventory/inventory.models';
import { Ticket } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';

@Component({
  selector: 'app-reporting-page',
  imports: [CommonModule, MatButtonModule, BaseChartDirective],
  templateUrl: './reporting.page.html',
  styleUrl: './reporting.page.scss',
})
export class ReportingPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);
  private readonly interventionsService = inject(InterventionsService);
  private readonly inventoryService = inject(InventoryService);
  private readonly ticketsService = inject(TicketsService);

  protected readonly loading = signal(true);
  protected readonly exporting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly totalTickets = signal(0);
  protected readonly openTickets = signal(0);
  protected readonly resolvedTickets = signal(0);
  protected readonly interventions = signal(0);

  protected readonly volumeChart = signal<ChartConfiguration<'line'>['data']>(this.emptyLine());
  protected readonly statusChart = signal<ChartConfiguration<'bar'>['data']>(this.emptyBar('Tickets'));
  protected readonly secondaryChart = signal<ChartConfiguration<'bar'>['data']>(this.emptyBar('Interventions'));
  protected readonly isAdmin = computed(() => this.auth.currentUser()?.role === 'ADMIN');

  protected readonly chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { labels: { color: 'rgba(255,255,255,0.72)' } },
    },
    scales: {
      x: { ticks: { color: 'rgba(255,255,255,0.62)' }, grid: { color: 'rgba(255,255,255,0.06)' } },
      y: { ticks: { color: 'rgba(255,255,255,0.62)' }, grid: { color: 'rgba(255,255,255,0.06)' } },
    },
  };

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    const role = this.auth.currentUser()?.role;

    if (role === 'ADMIN') {
      this.dashboardService.getAdminDashboard().subscribe({
        next: (data) => {
          this.totalTickets.set(data.totalTickets);
          this.openTickets.set(data.ticketsOuverts);
          this.resolvedTickets.set(data.ticketsResolus);
          this.interventions.set(data.totalInterventions);
          this.volumeChart.set(this.lineFromVolume(data.volumeTicketsParJour));
          this.statusChart.set(this.barFromCounts(data.ticketsParStatut, 'Tickets par statut', '#b7ff1f'));
          this.secondaryChart.set(this.barFromCounts(data.interventionsParStatut, 'Interventions', '#38bdf8'));
          this.loading.set(false);
        },
        error: () => this.fail(),
      });
      return;
    }

    if (this.isTechnician(role)) {
      this.dashboardService.getTechnicianDashboard().subscribe({
        next: (data) => {
          this.totalTickets.set(data.ticketsAssignes);
          this.openTickets.set(data.ticketsEnCours);
          this.resolvedTickets.set(data.ticketsFileNiveau);
          this.interventions.set(data.interventionsPlanifiees);
          this.volumeChart.set(this.emptyLine());
          this.statusChart.set(this.barFromCounts(data.ticketsAssignesParStatut, 'Mes tickets', '#b7ff1f'));
          this.secondaryChart.set(this.barFromCounts(data.interventionsParStatut, 'Mes interventions', '#38bdf8'));
          this.loading.set(false);
        },
        error: () => this.fail(),
      });
      return;
    }

    this.dashboardService.getRequesterDashboard().subscribe({
      next: (data) => {
        this.totalTickets.set(data.totalTickets);
        this.openTickets.set(data.ticketsOuverts);
        this.resolvedTickets.set(data.ticketsResolus);
        this.interventions.set(0);
        this.volumeChart.set(this.lineFromVolume(data.volumeTicketsParJour));
        this.statusChart.set(this.barFromCounts(data.ticketsParStatut, 'Mes tickets', '#b7ff1f'));
        this.secondaryChart.set(this.emptyBar('Non applicable'));
        this.loading.set(false);
      },
      error: () => this.fail(),
    });
  }

  protected exportTickets(): void {
    if (!this.isAdmin()) {
      this.error.set('Export incidents reserve a l administrateur.');
      return;
    }

    if (this.exporting()) {
      return;
    }

    this.exporting.set(true);
    this.error.set(null);

    this.ticketsService.list().subscribe({
      next: (tickets) => {
        this.downloadCsv(tickets);
        this.exporting.set(false);
      },
      error: () => {
        this.exporting.set(false);
        this.error.set('Export impossible. Verifiez votre session et vos droits.');
      },
    });
  }

  protected exportStock(): void {
    if (this.exporting()) {
      return;
    }

    this.exporting.set(true);
    this.error.set(null);

    this.inventoryService.listParts().subscribe({
      next: (parts) => {
        this.downloadStockCsv(parts);
        this.exporting.set(false);
      },
      error: () => {
        this.exporting.set(false);
        this.error.set('Export stock impossible. Verifiez votre session et vos droits.');
      },
    });
  }

  protected exportInterventions(): void {
    if (this.exporting()) {
      return;
    }

    this.exporting.set(true);
    this.error.set(null);

    this.interventionsService.list().subscribe({
      next: (interventions) => {
        this.downloadInterventionsCsv(interventions);
        this.exporting.set(false);
      },
      error: () => {
        this.exporting.set(false);
        this.error.set('Export interventions impossible. Verifiez votre session et vos droits.');
      },
    });
  }

  private isTechnician(role: UserRole | undefined): boolean {
    return role === 'TECH_N1' || role === 'TECH_N2' || role === 'TECH_N3';
  }

  private fail(): void {
    this.loading.set(false);
    this.error.set('Impossible de charger les rapports.');
  }

  private lineFromVolume(rows: DailyTicketVolume[]): ChartConfiguration<'line'>['data'] {
    return {
      labels: rows.map((row) => row.date),
      datasets: [
        {
          label: 'Volume tickets',
          data: rows.map((row) => row.total),
          borderColor: '#b7ff1f',
          backgroundColor: 'rgba(183, 255, 31, 0.14)',
          fill: true,
          tension: 0.35,
        },
      ],
    };
  }

  private barFromCounts(rows: CountByLabel[], label: string, color: string): ChartConfiguration<'bar'>['data'] {
    return {
      labels: rows.map((row) => row.libelle),
      datasets: [
        {
          label,
          data: rows.map((row) => row.total),
          backgroundColor: color,
          barPercentage: 0.42,
          categoryPercentage: 0.58,
          maxBarThickness: 34,
          borderRadius: 6,
        },
      ],
    };
  }

  private emptyLine(): ChartConfiguration<'line'>['data'] {
    return { labels: [], datasets: [{ label: 'Volume tickets', data: [], borderColor: '#b7ff1f' }] };
  }

  private emptyBar(label: string): ChartConfiguration<'bar'>['data'] {
    return {
      labels: [],
      datasets: [{
        label,
        data: [],
        backgroundColor: '#b7ff1f',
        barPercentage: 0.42,
        categoryPercentage: 0.58,
        maxBarThickness: 34,
        borderRadius: 6,
      }],
    };
  }

  private downloadCsv(tickets: Ticket[]): void {
    const headers = [
      'Reference',
      'Titre',
      'Description',
      'Categorie',
      'Priorite',
      'Statut',
      'Niveau',
      'Demandeur',
      'Telephone demandeur',
      'Technicien assigne',
      'Date creation',
      'Date resolution',
      'Date cloture',
      'Duree resolution',
      'Code achevement / commentaire resolution',
      'Feedback cloture',
      'Derniere modification',
    ];

    const rows = tickets.map((ticket) => [
      ticket.reference,
      ticket.titre,
      ticket.description,
      ticket.categorie,
      ticket.priorite,
      ticket.statut,
      ticket.niveauCourant,
      ticket.demandeurNomComplet,
      ticket.demandeurTelephone || '',
      ticket.technicienAssigneNomComplet || 'Non assigne',
      this.formatDateTime(ticket.dateCreation),
      this.formatDateTime(ticket.dateResolution),
      this.formatDateTime(ticket.dateCloture),
      this.resolutionDuration(ticket),
      ticket.commentaireResolution || ticket.codeAchevement || '',
      ticket.feedbackCloture || '',
      this.formatDateTime(ticket.dateDerniereModification),
    ]);

    const content = [headers, ...rows]
      .map((row) => row.map((value) => this.csvEscape(value)).join(';'))
      .join('\r\n');
    const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `liste-incidents-jusqu-au-${this.todayIsoDate()}.csv`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private downloadStockCsv(parts: SparePart[]): void {
    const headers = [
      'Reference',
      'Designation',
      'Description',
      'Quantite disponible',
      'Seuil alerte',
      'Statut stock',
      'Reference active',
      'Date creation',
    ];
    const rows = parts.map((part) => [
      part.reference,
      part.nom,
      part.description || '',
      part.quantiteDisponible,
      part.seuilAlerte,
      part.lowStock ? 'ALERTE' : 'OK',
      part.actif ? 'Oui' : 'Non',
      this.formatDateTime(part.dateCreation),
    ]);
    this.downloadGenericCsv('liste-stock', headers, rows);
  }

  private downloadInterventionsCsv(interventions: Intervention[]): void {
    const headers = [
      'ID',
      'Ticket',
      'Sujet ticket',
      'Technicien',
      'Statut',
      'Lieu',
      'Debut prevu',
      'Fin prevue',
      'Debut reel',
      'Fin reelle',
      'Rapport / raison',
      'Date creation',
    ];
    const rows = interventions.map((intervention) => [
      intervention.id,
      intervention.ticketReference,
      intervention.ticketTitre,
      intervention.technicienNomComplet,
      intervention.statut,
      intervention.lieu,
      this.formatDateTime(intervention.dateDebutPrevue),
      this.formatDateTime(intervention.dateFinPrevue),
      this.formatDateTime(intervention.dateDebutReelle),
      this.formatDateTime(intervention.dateFinReelle),
      intervention.rapport || '',
      this.formatDateTime(intervention.dateCreation),
    ]);
    this.downloadGenericCsv('liste-interventions', headers, rows);
  }

  private downloadGenericCsv(name: string, headers: string[], rows: Array<Array<string | number>>): void {
    const content = [headers, ...rows]
      .map((row) => row.map((value) => this.csvEscape(value)).join(';'))
      .join('\r\n');
    const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${name}-jusqu-au-${this.todayIsoDate()}.csv`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private csvEscape(value: string | number): string {
    const text = String(value ?? '');
    return `"${text.replace(/"/g, '""')}"`;
  }

  private formatDateTime(value: string | null): string {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  }

  private todayIsoDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private resolutionDuration(ticket: Ticket): string {
    if (!ticket.dateResolution) {
      return '';
    }

    const createdAt = new Date(ticket.dateCreation).getTime();
    const resolvedAt = new Date(ticket.dateResolution).getTime();
    const totalMinutes = Math.max(0, Math.round((resolvedAt - createdAt) / 60000));
    const days = Math.floor(totalMinutes / 1440);
    const hours = Math.floor((totalMinutes % 1440) / 60);
    const minutes = totalMinutes % 60;

    if (days > 0) {
      return `${days}j ${hours}h`;
    }
    if (hours > 0) {
      return `${hours}h ${minutes}min`;
    }
    return `${minutes}min`;
  }
}
