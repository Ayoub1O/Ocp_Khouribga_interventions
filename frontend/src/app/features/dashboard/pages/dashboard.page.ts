import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import {
  LucideActivity,
  LucideCheckCircle2,
  LucideClock3,
  LucideInfo,
  LucidePackageOpen,
  LucidePlus,
  LucideRefreshCw,
  LucideShieldCheck,
  LucideSparkles,
  LucideTicket,
  LucideWrench,
} from '@lucide/angular';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/auth/auth.models';
import { DashboardService } from '../../../core/dashboard/dashboard.service';
import { AdminDashboardData, RequesterDashboardData, TechnicianDashboardData } from '../../../core/dashboard/dashboard.models';
import { AppNotification } from '../../../core/notifications/notifications.models';
import { NotificationsService } from '../../../core/notifications/notifications.service';
import { Ticket } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    BaseChartDirective,
    LucideActivity,
    LucideCheckCircle2,
    LucideClock3,
    LucideInfo,
    LucidePackageOpen,
    LucidePlus,
    LucideRefreshCw,
    LucideShieldCheck,
    LucideSparkles,
    LucideTicket,
    LucideWrench,
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);
  private readonly notificationsService = inject(NotificationsService);
  private readonly ticketsService = inject(TicketsService);

  protected readonly currentUser = this.auth.currentUser;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly requesterData = signal<RequesterDashboardData | null>(null);
  protected readonly technicianData = signal<TechnicianDashboardData | null>(null);
  protected readonly adminData = signal<AdminDashboardData | null>(null);
  protected readonly recentActivity = signal<AppNotification[]>([]);
  protected readonly recentTickets = signal<Ticket[]>([]);

  protected readonly doughnutOptions: ChartOptions<'doughnut'> = {
    cutout: '70%',
    plugins: {
      legend: {
        position: 'right',
        labels: { color: '#dff7d2', boxWidth: 9, boxHeight: 9, usePointStyle: true },
      },
    },
  };

  protected readonly barOptions: ChartOptions<'bar'> = {
    maintainAspectRatio: false,
    scales: {
      x: {
        grid: { color: 'rgba(183, 255, 31, 0.08)' },
        ticks: { color: 'rgba(255, 255, 255, 0.72)' },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(183, 255, 31, 0.1)' },
        ticks: { color: 'rgba(255, 255, 255, 0.72)', precision: 0 },
      },
    },
    plugins: {
      legend: { display: false },
    },
  };

  protected readonly statusChartData = signal<ChartConfiguration<'doughnut'>['data']>({
    labels: [],
    datasets: [{ data: [], backgroundColor: ['#98df38', '#ffd21f', '#35c4a5', '#56a8ff'] }],
  });

  protected readonly secondaryChartData = signal<ChartConfiguration<'bar'>['data']>({
    labels: [],
    datasets: [{
      label: 'Tickets',
      data: [],
      backgroundColor: '#6fbd38',
      barPercentage: 0.42,
      categoryPercentage: 0.58,
      maxBarThickness: 34,
      borderRadius: 4,
    }],
  });

  protected readonly dailyVolumeChartData = signal<ChartConfiguration<'bar'>['data']>({
    labels: [],
    datasets: [{
      label: 'Tickets',
      data: [],
      backgroundColor: '#6fbd38',
      barPercentage: 0.42,
      categoryPercentage: 0.58,
      maxBarThickness: 34,
      borderRadius: 4,
    }],
  });

  ngOnInit(): void {
    this.fetchDashboardData();
  }

  get userRole(): UserRole {
    return this.currentUser()?.role || 'DEMANDEUR';
  }

  get isRequester(): boolean {
    return this.userRole === 'DEMANDEUR';
  }

  get isAdmin(): boolean {
    return this.userRole === 'ADMIN';
  }

  get isTechnician(): boolean {
    return this.userRole.startsWith('TECH_');
  }

  protected fetchDashboardData(): void {
    this.loading.set(true);
    this.error.set(null);
    this.fetchRecentActivity();
    this.fetchRecentTickets();

    if (this.isRequester) {
      this.dashboardService.getRequesterDashboard().subscribe({
        next: (data) => {
          this.requesterData.set(data);
          this.setupRequesterCharts(data);
          this.loading.set(false);
        },
        error: () => this.handleError(),
      });
      return;
    }

    if (this.isAdmin) {
      this.dashboardService.getAdminDashboard().subscribe({
        next: (data) => {
          this.adminData.set(data);
          this.setupAdminCharts(data);
          this.loading.set(false);
        },
        error: () => this.handleError(),
      });
      return;
    }

    this.dashboardService.getTechnicianDashboard().subscribe({
      next: (data) => {
        this.technicianData.set(data);
        this.setupTechnicianCharts(data);
        this.loading.set(false);
      },
      error: () => this.handleError(),
    });
  }

  private setupRequesterCharts(data: RequesterDashboardData): void {
    this.statusChartData.set({
      labels: data.ticketsParStatut.map((item) => item.libelle),
      datasets: [{
        data: data.ticketsParStatut.map((item) => item.total),
        backgroundColor: ['#98df38', '#ffd21f', '#35c4a5', '#56a8ff'],
        borderColor: '#071814',
        borderWidth: 2,
      }],
    });
    this.setupDailyVolumeChart(data.volumeTicketsParJour);
  }

  private setupAdminCharts(data: AdminDashboardData): void {
    this.statusChartData.set({
      labels: data.ticketsParStatut.map((item) => item.libelle),
      datasets: [{
        data: data.ticketsParStatut.map((item) => item.total),
        backgroundColor: ['#98df38', '#ffd21f', '#35c4a5', '#56a8ff'],
        borderColor: '#071814',
        borderWidth: 2,
      }],
    });

    this.secondaryChartData.set({
      labels: data.ticketsParNiveau.map((item) => item.libelle),
      datasets: [{
        label: 'Tickets',
        data: data.ticketsParNiveau.map((item) => item.total),
        backgroundColor: '#6fbd38',
        borderColor: '#b7ff1f',
        borderWidth: 1,
        barPercentage: 0.42,
        categoryPercentage: 0.58,
        maxBarThickness: 34,
        borderRadius: 4,
      }],
    });
    this.setupDailyVolumeChart(data.volumeTicketsParJour);
  }

  private setupTechnicianCharts(data: TechnicianDashboardData): void {
    this.statusChartData.set({
      labels: data.ticketsAssignesParStatut.map((item) => item.libelle),
      datasets: [{
        data: data.ticketsAssignesParStatut.map((item) => item.total),
        backgroundColor: ['#98df38', '#ffd21f', '#35c4a5', '#56a8ff'],
        borderColor: '#071814',
        borderWidth: 2,
      }],
    });

    this.secondaryChartData.set({
      labels: data.interventionsParStatut.map((item) => item.libelle),
      datasets: [{
        label: 'Interventions',
        data: data.interventionsParStatut.map((item) => item.total),
        backgroundColor: '#6fbd38',
        borderColor: '#b7ff1f',
        borderWidth: 1,
        barPercentage: 0.42,
        categoryPercentage: 0.58,
        maxBarThickness: 34,
        borderRadius: 4,
      }],
    });
  }

  private setupDailyVolumeChart(volume: { date: string; total: number }[]): void {
    this.dailyVolumeChartData.set({
      labels: volume.map((item) => this.formatDayLabel(item.date)),
      datasets: [{
        label: 'Tickets',
        data: volume.map((item) => item.total),
        backgroundColor: '#6fbd38',
        borderColor: '#b7ff1f',
        borderWidth: 1,
        barPercentage: 0.42,
        categoryPercentage: 0.58,
        maxBarThickness: 34,
        borderRadius: 4,
      }],
    });
  }

  private formatDayLabel(date: string): string {
    return new Intl.DateTimeFormat('fr-FR', { weekday: 'short' }).format(new Date(`${date}T00:00:00`));
  }

  private handleError(): void {
    this.loading.set(false);
    this.error.set('Impossible de charger les donnees du tableau de bord.');
  }

  private fetchRecentActivity(): void {
    this.notificationsService.list().subscribe({
      next: (notifications) => {
        this.recentActivity.set(
          [...notifications]
            .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
            .slice(0, 5),
        );
      },
      error: () => this.recentActivity.set([]),
    });
  }

  private fetchRecentTickets(): void {
    this.ticketsService.list().subscribe({
      next: (tickets) => {
        this.recentTickets.set(
          [...tickets]
            .sort((left, right) =>
              new Date(right.dateDerniereModification).getTime() - new Date(left.dateDerniereModification).getTime(),
            )
            .slice(0, 5),
        );
      },
      error: () => this.recentTickets.set([]),
    });
  }
}
