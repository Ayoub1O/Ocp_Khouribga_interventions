import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideSend } from '@lucide/angular';
import { CreateTicketRequest, TicketCategory, TicketPriority } from '../../../core/tickets/tickets.models';
import { TicketsService } from '../../../core/tickets/tickets.service';

@Component({
  selector: 'app-create-ticket-page',
  imports: [CommonModule, FormsModule, RouterLink, LucideArrowLeft, LucideSend],
  templateUrl: './create-ticket.page.html',
  styleUrl: './create-ticket.page.scss',
})
export class CreateTicketPage {
  private readonly ticketsService = inject(TicketsService);
  private readonly router = inject(Router);

  protected readonly categories: TicketCategory[] = ['MATERIEL', 'LOGICIEL', 'RESEAU', 'COMPTE_ACCES', 'EMAIL', 'IMPRIMANTE', 'SECURITE', 'AUTRE'];
  protected readonly priorities: TicketPriority[] = ['BASSE', 'NORMALE', 'HAUTE', 'CRITIQUE'];
  protected saving = false;
  protected error: string | null = null;

  protected ticket: CreateTicketRequest = {
    titre: '',
    description: '',
    categorie: 'AUTRE',
    priorite: 'NORMALE',
  };

  protected submit(): void {
    this.saving = true;
    this.error = null;

    this.ticketsService.create({
      titre: this.ticket.titre.trim(),
      description: this.ticket.description.trim(),
      categorie: this.ticket.categorie,
      priorite: this.ticket.priorite,
    }).subscribe({
      next: () => {
        this.saving = false;
        void this.router.navigateByUrl('/tickets');
      },
      error: () => {
        this.saving = false;
        this.error = 'Creation impossible. Verifiez les champs du ticket.';
      },
    });
  }
}
