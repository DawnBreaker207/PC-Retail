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
    // API thật trước, rớt về mock khi chưa có BE (giữ dev chạy được)
    return this.http.post<AuthResponse>(ApiEndpoints.auth.login, credentials).pipe(
      tap((res) => this.setSession(res)),
      catchError(() => of(this.buildMockSession(credentials.email)).pipe(
        tap((res) => this.setSession(res)),
      )),
    );
  }

  private buildMockSession(email: string): AuthResponse {
    const mockUser: User = {
      id: 1,
      fullName: email === 'admin@dawn.vn' ? 'Admin' : email.split('@')[0],
      email,
      phone: '0912345678',
      systemRole: email === 'admin@dawn.vn' ? 'ADMIN' : 'CUSTOMER',
    };
    return {
      accessToken: 'mock-token-' + Date.now(),
      refreshToken: 'mock-refresh-' + Date.now(),
      user: mockUser,
    };
  }

  register(data: RegisterRequest): Observable<User> {
    const mockUser: User = {
      id: Date.now(),
      fullName: data.fullName,
      email: data.email,
      phone: data.phone,
      systemRole: 'CUSTOMER',
    };
    return of(mockUser);
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
    this.currentUser.set(res.user);
    writeStoredSession({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
  }
}
