import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { tickets } from '../../../core/api/mock-data';

@Component({
  selector: 'app-tickets-page',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './tickets.page.html',
  styleUrl: './tickets.page.scss',
})
export class TicketsPage {
  protected readonly search = signal('');
  protected readonly level = signal('TOUS');
  protected readonly tickets = computed(() => {
    const query = this.search().toLowerCase().trim();
    return tickets.filter((ticket) => {
      const matchesQuery = !query || `${ticket.reference} ${ticket.titre} ${ticket.demandeur}`.toLowerCase().includes(query);
      const matchesLevel = this.level() === 'TOUS' || ticket.niveau === this.level();
      return matchesQuery && matchesLevel;
    });
  });
}
