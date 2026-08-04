import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppNotification } from './notifications.models';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>('/api/notifications');
  }

  markRead(id: string): Observable<AppNotification> {
    return this.http.post<AppNotification>(`/api/notifications/${id}/read`, {});
  }
}
