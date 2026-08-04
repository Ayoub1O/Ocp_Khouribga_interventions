import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InviteUserRequest, PendingInvitation, UserAccount } from './users.models';

@Injectable({ providedIn: 'root' })
export class UsersService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>('/api/users');
  }

  invite(request: InviteUserRequest): Observable<UserAccount> {
    return this.http.post<UserAccount>('/api/users/invitations', request);
  }

  pendingInvitations(): Observable<PendingInvitation[]> {
    return this.http.get<PendingInvitation[]>('/api/users/invitations/pending');
  }
}
