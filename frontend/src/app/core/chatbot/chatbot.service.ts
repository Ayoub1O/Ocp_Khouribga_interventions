import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatbotAnswer, ChatbotMessage, ChatbotSession } from './chatbot.models';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  constructor(private readonly http: HttpClient) {}

  startSession(messageInitial?: string): Observable<ChatbotSession> {
    return this.http.post<ChatbotSession>('/api/chatbot/sessions', { messageInitial });
  }

  messages(sessionId: string): Observable<ChatbotMessage[]> {
    return this.http.get<ChatbotMessage[]>(`/api/chatbot/sessions/${sessionId}/messages`);
  }

  sendMessage(sessionId: string, message: string): Observable<ChatbotAnswer> {
    return this.http.post<ChatbotAnswer>(`/api/chatbot/sessions/${sessionId}/messages`, { message });
  }

  confirmResolution(sessionId: string): Observable<ChatbotSession> {
    return this.http.post<ChatbotSession>(`/api/chatbot/sessions/${sessionId}/confirm-resolution`, {});
  }

  escalate(sessionId: string): Observable<ChatbotAnswer> {
    return this.http.post<ChatbotAnswer>(`/api/chatbot/sessions/${sessionId}/escalate`, {});
  }
}
