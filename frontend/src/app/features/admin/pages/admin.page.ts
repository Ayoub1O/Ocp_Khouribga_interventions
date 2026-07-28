import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-admin-page',
  imports: [MatButtonModule],
  templateUrl: './admin.page.html',
  styleUrl: './admin.page.scss',
})
export class AdminPage {
  protected readonly actions = [
    { title: 'Utilisateurs', description: 'Invitations, roles et activation des comptes.' },
    { title: 'Base de connaissances', description: 'Articles N0, imports, validation et embeddings.' },
    { title: 'Parametres securite', description: 'JWT, SMTP, journalisation et politiques d acces.' },
  ];
}
