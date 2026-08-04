import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { SupportLevel, Ticket } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';

type QueueBucket = {
  level: SupportLevel;
  title: string;
  tickets: Ticket[];
};

@Component({
  selector: 'app-queues-page',
  imports: [CommonModule, MatButtonModule],
  templateUrl: './queues.page.html',
  styleUrl: './queues.page.scss',
})
export class QueuesPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly ticketsService = inject(TicketsService);

  protected readonly tickets = signal<Ticket[]>([]);
  protected readonly loading = signal(true);
  protected readonly claimingId = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly level = computed<SupportLevel>(() => this.levelForRole());
  protected readonly queues = signal<QueueBucket[]>([]);
  protected readonly isAdmin = computed(() => this.auth.currentUser()?.role === 'ADMIN');

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    if (this.isAdmin()) {
      this.loadAdminQueues();
      return;
    }

    this.ticketsService.queue(this.level()).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger la file support.');
      },
    });
  }

  protected claim(ticket: Ticket): void {
    if (this.claimingId()) {
      return;
    }

    this.claimingId.set(ticket.id);
    this.error.set(null);
    this.success.set(null);

    this.ticketsService.claim(ticket.id).subscribe({
      next: () => {
        this.claimingId.set(null);
        this.success.set(`Ticket ${ticket.reference} adopte.`);
        this.load();
      },
      error: () => {
        this.claimingId.set(null);
        this.error.set('Adoption impossible. Le ticket a peut-etre deja ete pris.');
      },
    });
  }

  private levelForRole(): SupportLevel {
    const role = this.auth.currentUser()?.role;
    if (role === 'TECH_N2') {
      return 'N2';
    }
    if (role === 'TECH_N3') {
      return 'N3';
    }
    return 'N1';
  }

  private loadAdminQueues(): void {
    forkJoin({
      n1: this.ticketsService.queue('N1'),
      n2: this.ticketsService.queue('N2'),
      n3: this.ticketsService.queue('N3'),
    }).subscribe({
      next: ({ n1, n2, n3 }) => {
        this.queues.set([
          { level: 'N1', title: 'Support distant', tickets: n1 },
          { level: 'N2', title: 'Diagnostic sur site', tickets: n2 },
          { level: 'N3', title: 'Intervention avancee', tickets: n3 },
        ]);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger les files support.');
      },
    });
  }
}
