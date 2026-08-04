import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/auth/auth.models';
import { DashboardService } from '../../../core/dashboard/dashboard.service';
import { CountByLabel, DailyTicketVolume } from '../../../core/dashboard/dashboard.models';

@Component({
  selector: 'app-reporting-page',
  imports: [CommonModule, MatButtonModule, BaseChartDirective],
  templateUrl: './reporting.page.html',
  styleUrl: './reporting.page.scss',
})
export class ReportingPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);

  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly totalTickets = signal(0);
  protected readonly openTickets = signal(0);
  protected readonly resolvedTickets = signal(0);
  protected readonly interventions = signal(0);

  protected readonly volumeChart = signal<ChartConfiguration<'line'>['data']>(this.emptyLine());
  protected readonly statusChart = signal<ChartConfiguration<'bar'>['data']>(this.emptyBar('Tickets'));
  protected readonly secondaryChart = signal<ChartConfiguration<'bar'>['data']>(this.emptyBar('Interventions'));

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
          borderRadius: 6,
        },
      ],
    };
  }

  private emptyLine(): ChartConfiguration<'line'>['data'] {
    return { labels: [], datasets: [{ label: 'Volume tickets', data: [], borderColor: '#b7ff1f' }] };
  }

  private emptyBar(label: string): ChartConfiguration<'bar'>['data'] {
    return { labels: [], datasets: [{ label, data: [], backgroundColor: '#b7ff1f', borderRadius: 6 }] };
  }
}
