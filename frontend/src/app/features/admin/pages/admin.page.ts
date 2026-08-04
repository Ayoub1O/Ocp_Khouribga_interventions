import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  LucideCheckCircle2,
  LucideClock3,
  LucideMailPlus,
  LucideShieldCheck,
  LucideTicket,
  LucideUserCog,
  LucideUsers,
  LucideWrench,
} from '@lucide/angular';
import { DashboardService } from '../../../core/dashboard/dashboard.service';
import { AdminDashboardData } from '../../../core/dashboard/dashboard.models';
import { InviteUserRequest, PendingInvitation, UserAccount } from '../../../core/users/users.models';
import { UsersService } from '../../../core/users/users.service';

@Component({
  selector: 'app-admin-page',
  imports: [
    CommonModule,
    FormsModule,
    LucideCheckCircle2,
    LucideClock3,
    LucideMailPlus,
    LucideShieldCheck,
    LucideTicket,
    LucideUserCog,
    LucideUsers,
    LucideWrench,
  ],
  templateUrl: './admin.page.html',
  styleUrl: './admin.page.scss',
})
export class AdminPage implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly usersService = inject(UsersService);

  protected readonly dashboard = signal<AdminDashboardData | null>(null);
  protected readonly users = signal<UserAccount[]>([]);
  protected readonly pendingTechnicianInvitations = signal<PendingInvitation[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  protected invitation: InviteUserRequest = {
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    role: 'TECH_N1',
  };

  protected readonly technicianAccounts = computed(() =>
    this.users().filter((user) => user.role === 'TECH_N1' || user.role === 'TECH_N2' || user.role === 'TECH_N3'),
  );

  protected readonly adminAccounts = computed(() =>
    this.users().filter((user) => user.role === 'ADMIN'),
  );

  protected readonly activeTechnicians = computed(() =>
    this.technicianAccounts().filter((user) => user.actif && user.emailVerified),
  );

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.dashboardService.getAdminDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
      },
      error: () => {
        this.error.set('Impossible de charger le tableau de bord administrateur.');
      },
    });
    this.loadUsers();
    this.loadPendingInvitations();
  }

  protected invite(): void {
    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.usersService.invite({
      nom: this.invitation.nom.trim(),
      prenom: this.invitation.prenom.trim(),
      email: this.invitation.email.trim(),
      telephone: this.invitation.telephone?.trim(),
      role: this.invitation.role,
    }).subscribe({
      next: () => {
        this.success.set('Invitation envoyee au technicien.');
        this.invitation = { nom: '', prenom: '', email: '', telephone: '', role: 'TECH_N1' };
        this.saving.set(false);
        this.loadUsers();
        this.loadPendingInvitations();
      },
      error: (response: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(response.error?.message ?? 'Invitation impossible. Verifiez les champs et le role choisi.');
      },
    });
  }

  private loadUsers(): void {
    this.usersService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger les comptes utilisateurs.');
      },
    });
  }

  private loadPendingInvitations(): void {
    this.usersService.pendingInvitations().subscribe({
      next: (invitations) => this.pendingTechnicianInvitations.set(invitations),
      error: () => this.pendingTechnicianInvitations.set([]),
    });
  }
}
