import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, Observable, of, catchError, finalize, shareReplay } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User, LoginRequest, AuthResponse, RegisterRequest } from '@core/models/common.model';

const SESSION_STORAGE_KEY = 'pc-retail-session';

interface StoredSession {
  accessToken: string;
  refreshToken: string;
  user: User;
}

function readStoredSession(): StoredSession | null {
  try {
    const raw = localStorage.getItem(SESSION_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as StoredSession) : null;
  } catch {
    return null;
  }
}

function decodeJwtUser(token: string): User | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    const rawRole: string = payload.role || '';
    let systemRole: User['systemRole'] = 'CUSTOMER';
    if (rawRole.includes('ROLE_ADMIN') || rawRole.includes('ADMIN')) systemRole = 'ADMIN';
    else if (rawRole.includes('ROLE_STAFF') || rawRole.includes('STAFF')) systemRole = 'STAFF';
    return {
      id: payload.id || 1,
      fullName: payload.username?.split('@')[0] || payload.email?.split('@')[0] || 'User',
      email: payload.email || payload.sub,
      systemRole,
    };
  } catch {
    return null;
  }
}

function writeStoredSession(session: StoredSession | null): void {
  try {
    if (session) localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
    else localStorage.removeItem(SESSION_STORAGE_KEY);
  } catch {
    // ignore storage errors (private mode, quota, etc.)
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private stored = readStoredSession();
  currentUser = signal<User | null>(this.stored?.user ?? null);
  private accessToken: string | null = this.stored?.accessToken ?? null;
  private refreshToken: string | null = this.stored?.refreshToken ?? null;

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(ApiEndpoints.auth.login, credentials).pipe(
      tap((res) => this.setSession(res)),
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(ApiEndpoints.auth.register, data).pipe(
      tap((res) => this.setSession(res)),
    );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(ApiEndpoints.auth.forgotPassword, { email });
  }

  logout(): void {
    this.accessToken = null;
    this.refreshToken = null;
    this.currentUser.set(null);
    writeStoredSession(null);
    this.router.navigate(['/auth/login']);
  }

  // Single-flight: nhiều 401 cùng lúc chỉ gọi refresh 1 lần
  private refresh$: Observable<AuthResponse> | null = null;

  refresh(): Observable<AuthResponse> {
    if (!this.refresh$) {
      this.refresh$ = this.http.post<AuthResponse>(ApiEndpoints.auth.refresh, {}).pipe(
        tap((res) => this.setSession(res)),
        finalize(() => {
          this.refresh$ = null;
        }),
        shareReplay(1),
      );
    }
    return this.refresh$;
  }

  getToken(): string | null {
    return this.accessToken;
  }

  isAuthenticated = computed(() => !!this.currentUser());

  private setSession(res: AuthResponse): void {
    this.accessToken = res.accessToken;
    this.refreshToken = res.refreshToken;
    const user = res.user ?? (res.accessToken ? decodeJwtUser(res.accessToken) : null);
    this.currentUser.set(user);
    if (user) {
      writeStoredSession({ accessToken: res.accessToken, refreshToken: res.refreshToken, user });
    }
  }
}
