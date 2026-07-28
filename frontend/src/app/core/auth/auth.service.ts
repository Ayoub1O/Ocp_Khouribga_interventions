import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { CurrentUser, LoginRequest, LoginResponse, RegisterRequest, MessageResponse } from './auth.models';

const ACCESS_TOKEN_KEY = 'itsm_access_token';
const REFRESH_TOKEN_KEY = 'itsm_refresh_token';
const CURRENT_USER_KEY = 'itsm_current_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userState = signal<CurrentUser | null>(this.restoreUser());
  readonly currentUser = this.userState.asReadonly();
  readonly isAuthenticated = computed(() => !!this.userState() && !!this.accessToken());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {
  }

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>('/api/auth/login', credentials).pipe(
      tap((response) => this.persistSession(response)),
    );
  }

  register(data: RegisterRequest) {
    return this.http.post<MessageResponse>('/api/auth/register', data);
  }

  verifyEmail(token: string) {
    return this.http.get<MessageResponse>('/api/auth/verify-email', {
      params: { token },
    });
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(CURRENT_USER_KEY);
    this.userState.set(null);
    void this.router.navigateByUrl('/login');
  }

  accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  private persistSession(response: LoginResponse): void {
    const user: CurrentUser = {
      id: response.userId,
      email: response.email,
      role: response.role,
    };

    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
    this.userState.set(user);
  }

  private restoreUser(): CurrentUser | null {
    const raw = localStorage.getItem(CURRENT_USER_KEY);
    if (!raw || !localStorage.getItem(ACCESS_TOKEN_KEY)) {
      return null;
    }

    try {
      return JSON.parse(raw) as CurrentUser;
    } catch {
      localStorage.removeItem(CURRENT_USER_KEY);
      return null;
    }
  }
}
