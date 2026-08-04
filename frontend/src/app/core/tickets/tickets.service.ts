import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CloseTicketRequest, CreateTicketRequest, EscalateTicketRequest, ResolveTicketRequest, SupportLevel, Ticket } from './tickets.models';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>('/api/tickets');
  }

  create(request: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>('/api/tickets', request);
  }

  queue(level: SupportLevel): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`/api/tickets/queues/${level}`);
  }

  claim(ticketId: string): Observable<Ticket> {
    return this.http.post<Ticket>(`/api/tickets/${ticketId}/claim`, {});
  }

  escalate(ticketId: string, request: EscalateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`/api/tickets/${ticketId}/escalate`, request);
  }

  resolve(ticketId: string, request: ResolveTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`/api/tickets/${ticketId}/resolve`, request);
  }

  close(ticketId: string, request: CloseTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`/api/tickets/${ticketId}/close`, request);
  }
}
