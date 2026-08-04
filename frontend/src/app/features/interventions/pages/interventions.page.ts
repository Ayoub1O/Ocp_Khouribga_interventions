import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import { LucideDownload, LucidePlay, LucidePlus, LucideRefreshCw, LucideX } from '@lucide/angular';
import { AuthService } from '../../../core/auth/auth.service';
import { InterventionsService } from '../../../core/interventions/interventions.service';
import { Intervention } from '../../../core/interventions/interventions.models';
import { Ticket } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';
import { UserAccount } from '../../../core/users/users.models';
import { UsersService } from '../../../core/users/users.service';

@Component({
  selector: 'app-interventions-page',
  imports: [
    CommonModule,
    FormsModule,
    FullCalendarModule,
    LucideDownload,
    LucidePlay,
    LucidePlus,
    LucideRefreshCw,
    LucideX,
  ],
  templateUrl: './interventions.page.html',
  styleUrl: './interventions.page.scss',
})
export class InterventionsPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly interventionsService = inject(InterventionsService);
  private readonly ticketsService = inject(TicketsService);
  private readonly usersService = inject(UsersService);

  protected readonly currentUser = this.auth.currentUser;
  protected readonly interventions = signal<Intervention[]>([]);
  protected readonly tickets = signal<Ticket[]>([]);
  protected readonly users = signal<UserAccount[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly selectedIntervention = signal<Intervention | null>(null);
  protected readonly actionMode = signal<'COMPLETE' | 'CANCEL' | null>(null);
  protected readonly actionText = signal('');

  protected readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  protected readonly technicianOptions = computed(() => {
    const technicians = this.users().filter((user) => user.role === 'TECH_N2' || user.role === 'TECH_N3');
    if (technicians.length > 0) {
      return technicians;
    }
    const user = this.currentUser();
    if (user?.role === 'TECH_N2' || user?.role === 'TECH_N3') {
      return [{
        id: user.id,
        nom: user.nom || '',
        prenom: user.prenom || user.email,
        email: user.email,
        telephone: user.telephone || null,
        role: user.role,
        actif: true,
        emailVerified: true,
        dateCreation: '',
      }];
    }
    return [];
  });

  protected readonly planningTicketOptions = computed(() =>
    this.tickets().filter((ticket) =>
      ticket.niveauCourant === 'N3'
      && (ticket.statut === 'OUVERT' || ticket.statut === 'EN_COURS' || ticket.statut === 'ESCALADE'),
    ),
  );

  protected readonly upcomingInterventions = computed(() =>
    this.interventions().filter((intervention) =>
      intervention.statut === 'PLANIFIEE' || intervention.statut === 'EN_COURS',
    ),
  );

  protected readonly archivedInterventions = computed(() =>
    this.interventions().filter((intervention) =>
      intervention.statut === 'TERMINEE' || intervention.statut === 'ANNULEE',
    ),
  );

  protected createForm = {
    ticketId: '',
    technicienId: '',
    dateDebutPrevue: '',
    dateFinPrevue: '',
    lieu: '',
  };

  protected readonly calendarOptions = computed<CalendarOptions>(() => ({
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    height: 620,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    locale: 'fr',
    events: this.interventions()
      .filter((intervention) => intervention.statut !== 'ANNULEE')
      .map((intervention) => ({
        id: intervention.id,
        title: `${this.ticketLabel(intervention)} - ${this.technicianLabel(intervention)}`,
        start: intervention.dateDebutPrevue,
        end: intervention.dateFinPrevue,
        classNames: [`status-${intervention.statut.toLowerCase()}`],
      })),
  }));

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.interventionsService.list().subscribe({
      next: (interventions) => {
        this.interventions.set(interventions);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les interventions.');
        this.loading.set(false);
      },
    });

    this.ticketsService.list().subscribe({
      next: (tickets) => this.tickets.set(tickets),
      error: () => this.tickets.set([]),
    });

    this.usersService.list().subscribe({
      next: (users) => this.users.set(users),
      error: () => this.users.set([]),
    });
  }

  protected planIntervention(): void {
    if (this.saving()) {
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.interventionsService.create({
      ticketId: this.createForm.ticketId,
      technicienId: this.createForm.technicienId,
      dateDebutPrevue: this.toInstant(this.createForm.dateDebutPrevue),
      dateFinPrevue: this.toInstant(this.createForm.dateFinPrevue),
      lieu: this.createForm.lieu.trim(),
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Intervention planifiee.');
        this.createForm = { ticketId: '', technicienId: '', dateDebutPrevue: '', dateFinPrevue: '', lieu: '' };
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Planification impossible. Verifiez le ticket, le technicien et les dates.');
      },
    });
  }

  protected start(intervention: Intervention): void {
    if (this.saving()) {
      return;
    }
    this.saving.set(true);
    this.interventionsService.start(intervention.id).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Intervention demarree.');
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Demarrage impossible pour cette intervention.');
      },
    });
  }

  protected openAction(intervention: Intervention, mode: 'COMPLETE' | 'CANCEL'): void {
    this.selectedIntervention.set(intervention);
    this.actionMode.set(mode);
    this.actionText.set('');
    this.error.set(null);
    this.success.set(null);
  }

  protected cancelAction(): void {
    this.selectedIntervention.set(null);
    this.actionMode.set(null);
    this.actionText.set('');
  }

  protected submitAction(): void {
    const intervention = this.selectedIntervention();
    const mode = this.actionMode();
    const text = this.actionText().trim();
    if (!intervention || !mode || !text || this.saving()) {
      return;
    }

    this.saving.set(true);
    const operation = mode === 'COMPLETE'
      ? this.interventionsService.complete(intervention.id, { rapport: text })
      : this.interventionsService.cancel(intervention.id, { raison: text });

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(mode === 'COMPLETE' ? 'Intervention terminee.' : 'Intervention annulee.');
        this.cancelAction();
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Action impossible pour cette intervention.');
      },
    });
  }

  protected exportInterventions(): void {
    const headers = [
      'Intervention',
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
    const rows = this.interventions().map((intervention) => [
      this.shortId(intervention.id),
      this.ticketLabel(intervention),
      intervention.ticketTitre || '-',
      this.technicianLabel(intervention),
      intervention.statut,
      intervention.lieu,
      this.formatDateTime(intervention.dateDebutPrevue),
      this.formatDateTime(intervention.dateFinPrevue),
      this.formatDateTime(intervention.dateDebutReelle),
      this.formatDateTime(intervention.dateFinReelle),
      intervention.rapport || '',
      this.formatDateTime(intervention.dateCreation),
    ]);
    this.downloadCsv('liste-interventions', headers, rows);
  }

  protected canStart(intervention: Intervention): boolean {
    return intervention.statut === 'PLANIFIEE';
  }

  protected canComplete(intervention: Intervention): boolean {
    return intervention.statut === 'EN_COURS';
  }

  protected canCancel(intervention: Intervention): boolean {
    return intervention.statut !== 'TERMINEE' && intervention.statut !== 'ANNULEE';
  }

  protected actionTitle(): string {
    return this.actionMode() === 'COMPLETE' ? 'Terminer intervention' : 'Annuler intervention';
  }

  protected actionLabel(): string {
    return this.actionMode() === 'COMPLETE' ? 'Rapport d intervention' : 'Raison d annulation';
  }

  protected ticketLabel(intervention: Intervention): string {
    return intervention.ticketReference || this.shortId(intervention.ticketId);
  }

  protected technicianLabel(intervention: Intervention): string {
    return intervention.technicienNomComplet || 'Technicien non renseigne';
  }

  protected shortId(id: string): string {
    return id ? id.slice(0, 8).toUpperCase() : '-';
  }

  private toInstant(value: string): string {
    return new Date(value).toISOString();
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

  private downloadCsv(name: string, headers: string[], rows: Array<Array<string | number>>): void {
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

  private todayIsoDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
