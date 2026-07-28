import { Component, signal } from '@angular/core';
import { PercentPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';

type ChatMessage = {
  author: 'bot' | 'user';
  text: string;
  confidence?: number;
};

@Component({
  selector: 'app-chatbot-page',
  imports: [FormsModule, PercentPipe, MatButtonModule, MatInputModule, MatFormFieldModule],
  templateUrl: './chatbot.page.html',
  styleUrl: './chatbot.page.scss',
})
export class ChatbotPage {
  protected readonly draft = signal('');
  protected readonly messages = signal<ChatMessage[]>([
    { author: 'bot', text: 'Bonjour. Decrivez votre probleme informatique avec le plus de details possible.' },
    { author: 'user', text: 'Je n arrive pas a me connecter au VPN, erreur 809.' },
    {
      author: 'bot',
      text: 'Procedure proposee: verifier la connexion Internet, relancer le client VPN, puis controler le profil VPN. Si l erreur persiste, je peux creer un ticket N1 apres confirmation.',
      confidence: 0.78,
    },
  ]);

  protected send(): void {
    const value = this.draft().trim();
    if (!value) {
      return;
    }
    this.messages.update((messages) => [
      ...messages,
      { author: 'user', text: value },
      { author: 'bot', text: 'Analyse N0 en cours. Une reponse Gemini/RAG sera affichee ici.', confidence: 0.62 },
    ]);
    this.draft.set('');
  }
}
