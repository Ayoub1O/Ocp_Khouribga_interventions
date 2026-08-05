import { CommonModule, PercentPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  LucideBot,
  LucideCheckCircle2,
  LucideRefreshCw,
  LucideSend,
  LucideShieldAlert,
  LucideSparkles,
  LucideTicket,
} from '@lucide/angular';
import { ChatbotAnswer, ChatbotMessage, ChatbotSession } from '../../../core/chatbot/chatbot.models';
import { ChatbotService } from '../../../core/chatbot/chatbot.service';

@Component({
  selector: 'app-chatbot-page',
  imports: [
    CommonModule,
    FormsModule,
    PercentPipe,
    RouterLink,
    LucideBot,
    LucideCheckCircle2,
    LucideRefreshCw,
    LucideSend,
    LucideShieldAlert,
    LucideSparkles,
    LucideTicket,
  ],
  templateUrl: './chatbot.page.html',
  styleUrl: './chatbot.page.scss',
})
export class ChatbotPage implements OnInit {
  private readonly chatbotService = inject(ChatbotService);

  protected readonly draft = signal('');
  protected readonly messages = signal<ChatbotMessage[]>([]);
  protected readonly session = signal<ChatbotSession | null>(null);
  protected readonly lastAnswer = signal<ChatbotAnswer | null>(null);
  protected readonly loading = signal(true);
  protected readonly sending = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly isOpen = computed(() => this.session()?.statut === 'OUVERTE');
  protected readonly canEscalateToN1 = computed(() => this.isOpen() && !this.session()?.ticketId);

  ngOnInit(): void {
    this.start();
  }

  protected start(): void {
    this.loading.set(true);
    this.error.set(null);
    this.lastAnswer.set(null);

    this.chatbotService.startSession().subscribe({
      next: (session) => {
        this.session.set(session);
        this.loadMessages(session.id);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de demarrer AssistEX.');
      },
    });
  }

  protected send(): void {
    const value = this.draft().trim();
    const sessionId = this.session()?.id;
    if (!value || !sessionId || this.sending() || !this.isOpen()) {
      return;
    }

    this.sending.set(true);
    this.error.set(null);
    this.draft.set('');
    this.messages.update((messages) => [...messages, this.localUserMessage(value)]);

    this.chatbotService.sendMessage(sessionId, value).subscribe({
      next: (answer) => {
        this.lastAnswer.set(answer);
        this.session.set(answer.session);
        this.loadMessages(sessionId, false);
      },
      error: () => {
        this.sending.set(false);
        this.error.set('AssistEX n a pas pu traiter ce message.');
      },
    });
  }

  protected confirmResolution(): void {
    const sessionId = this.session()?.id;
    if (!sessionId || !this.isOpen()) {
      return;
    }

    this.chatbotService.confirmResolution(sessionId).subscribe({
      next: (session) => {
        this.session.set(session);
        this.loadMessages(sessionId, false);
      },
      error: () => this.error.set('Confirmation impossible.'),
    });
  }

  protected escalate(): void {
    const sessionId = this.session()?.id;
    if (!sessionId || !this.isOpen()) {
      return;
    }

    this.sending.set(true);
    this.chatbotService.escalate(sessionId).subscribe({
      next: (answer) => {
        this.lastAnswer.set(answer);
        this.session.set(answer.session);
        this.loadMessages(sessionId, false);
      },
      error: () => {
        this.sending.set(false);
        this.error.set('Escalade N1 impossible.');
      },
    });
  }

  protected authorLabel(message: ChatbotMessage): string {
    return message.auteur === 'UTILISATEUR' ? 'Vous' : message.auteur === 'SYSTEME' ? 'Systeme' : 'AssistEX';
  }

  private localUserMessage(content: string): ChatbotMessage {
    return {
      id: `local-${Date.now()}`,
      auteur: 'UTILISATEUR',
      contenu: content,
      sourcesUtilisees: null,
      confidenceScore: null,
      dateCreation: new Date().toISOString(),
    };
  }

  private loadMessages(sessionId: string, clearLoading = true): void {
    this.chatbotService.messages(sessionId).subscribe({
      next: (messages) => {
        this.messages.set(messages);
        this.loading.set(false);
        this.sending.set(false);
      },
      error: () => {
        if (clearLoading) {
          this.loading.set(false);
        }
        this.sending.set(false);
        this.error.set('Impossible de charger la conversation.');
      },
    });
  }
}
