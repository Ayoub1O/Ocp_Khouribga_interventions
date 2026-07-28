import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { tickets } from '../../../core/api/mock-data';

@Component({
  selector: 'app-queues-page',
  imports: [MatButtonModule],
  templateUrl: './queues.page.html',
  styleUrl: './queues.page.scss',
})
export class QueuesPage {
  protected readonly queues = [
    { level: 'N1', title: 'Support distant', count: 14, tickets: tickets.filter((ticket) => ticket.niveau === 'N1') },
    { level: 'N2', title: 'Diagnostic sur site', count: 6, tickets: tickets.filter((ticket) => ticket.niveau === 'N2') },
    { level: 'N3', title: 'Intervention avancee', count: 3, tickets: tickets.filter((ticket) => ticket.niveau === 'N3') },
  ];
}
