import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideBot, LucideEye, LucidePlus, LucideRefreshCw, LucideTicket, LucideX } from '@lucide/angular';
import { AuthService } from '../../../core/auth/auth.service';
import { SupportLevel, Ticket, TicketStatus } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';

@Component({
  selector: 'app-tickets-page',
  imports: [CommonModule, FormsModule, RouterLink, LucideBot, LucideEye, LucidePlus, LucideRefreshCw, LucideTicket, LucideX],
  templateUrl: './tickets.page.html',
  styleUrl: './tickets.page.scss',
})
export class TicketsPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly ticketsService = inject(TicketsService);

  protected readonly currentUser = this.auth.currentUser;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly level = signal<SupportLevel | 'TOUS'>('TOUS');
  protected readonly status = signal<TicketStatus | 'TOUS'>('TOUS');
  protected readonly tickets = signal<Ticket[]>([]);
  protected readonly selectedTicket = signal<Ticket | null>(null);
  protected readonly detailTicket = signal<Ticket | null>(null);
  protected readonly actionMode = signal<'ESCALATE' | 'RESOLVE' | 'CLOSE' | null>(null);
  protected readonly actionComment = signal('');
  protected readonly actionLoading = signal(false);

  protected readonly filteredTickets = computed(() =>
    this.tickets().filter((ticket) => {
      const matchesLevel = this.level() === 'TOUS' || ticket.niveauCourant === this.level();
      const matchesStatus = this.status() === 'TOUS' || ticket.statut === this.status();
      return matchesLevel && matchesStatus;
    }),
  );

  protected readonly isRequester = computed(() => this.currentUser()?.role === 'DEMANDEUR');
  protected readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');
  protected readonly isTechnician = computed(() => {
    const role = this.currentUser()?.role;
    return role === 'TECH_N1' || role === 'TECH_N2' || role === 'TECH_N3';
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.ticketsService.list().subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les tickets.');
        this.loading.set(false);
      },
    });
  }

  protected openAction(ticket: Ticket, mode: 'ESCALATE' | 'RESOLVE' | 'CLOSE'): void {
    this.selectedTicket.set(ticket);
    this.actionMode.set(mode);
    this.actionComment.set('');
    this.error.set(null);
    this.success.set(null);
  }

  protected cancelAction(): void {
    this.selectedTicket.set(null);
    this.actionMode.set(null);
    this.actionComment.set('');
  }

  protected submitAction(): void {
    const ticket = this.selectedTicket();
    const mode = this.actionMode();
    const comment = this.actionComment().trim();
    if (!ticket || !mode || !comment || this.actionLoading()) {
      return;
    }

    this.actionLoading.set(true);
    this.error.set(null);
    this.success.set(null);

    const operation = mode === 'ESCALATE'
      ? this.ticketsService.escalate(ticket.id, { raison: comment })
      : mode === 'RESOLVE'
        ? this.ticketsService.resolve(ticket.id, { commentaire: comment })
        : this.ticketsService.close(ticket.id, { commentaire: comment });

    operation.subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.success.set(this.successMessage(mode, ticket.reference));
        this.cancelAction();
        this.load();
      },
      error: () => {
        this.actionLoading.set(false);
        this.error.set('Action impossible. Verifiez le statut du ticket et vos droits.');
      },
    });
  }

  protected canEscalate(ticket: Ticket): boolean {
    return (this.isTechnician() || this.isAdmin()) && ticket.statut === 'EN_COURS' && ticket.niveauCourant !== 'N3';
  }

  protected canResolve(ticket: Ticket): boolean {
    return (this.isTechnician() || this.isAdmin()) && ticket.statut === 'EN_COURS';
  }

  protected canClose(ticket: Ticket): boolean {
    return (this.isRequester() || this.isAdmin()) && ticket.statut === 'RESOLU';
  }

  protected actionTitle(): string {
    return this.actionMode() === 'ESCALATE'
      ? 'Escalader le ticket'
      : this.actionMode() === 'RESOLVE'
        ? 'Marquer comme resolu'
        : 'Cloturer avec feedback';
  }

  protected actionPlaceholder(): string {
    return this.actionMode() === 'ESCALATE'
      ? 'Description du diagnostic et raison de l escalade...'
      : this.actionMode() === 'RESOLVE'
        ? 'Code d achevement / solution appliquee, controles effectues, resultat observe...'
        : 'Confirmation finale du demandeur, feedback ou remarque de cloture...';
  }

  protected completionComment(ticket: Ticket): string {
    return ticket.commentaireResolution || ticket.codeAchevement || '-';
  }

  protected assignmentLabel(ticket: Ticket): string {
    return ticket.technicienAssigneNomComplet || 'Non assigne';
  }

  protected resolutionDuration(ticket: Ticket): string {
    if (!ticket.dateResolution) {
      return '-';
    }

    const startedAt = new Date(ticket.dateCreation).getTime();
    const resolvedAt = new Date(ticket.dateResolution).getTime();
    const diffMinutes = Math.max(0, Math.round((resolvedAt - startedAt) / 60000));
    const days = Math.floor(diffMinutes / 1440);
    const hours = Math.floor((diffMinutes % 1440) / 60);
    const minutes = diffMinutes % 60;

    if (days > 0) {
      return `${days}j ${hours}h`;
    }
    if (hours > 0) {
      return `${hours}h ${minutes}min`;
    }
    return `${minutes}min`;
  }

  private successMessage(mode: 'ESCALATE' | 'RESOLVE' | 'CLOSE', reference: string): string {
    return mode === 'ESCALATE'
      ? `Ticket ${reference} escalade.`
      : mode === 'RESOLVE'
        ? `Ticket ${reference} marque comme resolu.`
        : `Ticket ${reference} cloture.`;
  }
}
