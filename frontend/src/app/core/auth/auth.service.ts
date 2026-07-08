import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, Observable, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User, LoginRequest, AuthResponse, RegisterRequest } from '@core/models/common.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  currentUser = signal<User | null>(null);
  private accessToken: string | null = null;

  login(credentials: LoginRequest): Observable<AuthResponse> {
    const mockUser: User = {
      id: 1,
      fullName: credentials.email === 'admin@dawn.vn' ? 'Admin' : credentials.email.split('@')[0],
      email: credentials.email,
      phone: '0912345678',
      systemRole: credentials.email === 'admin@dawn.vn' ? 'ADMIN' : 'CUSTOMER',
    };
    const mockRes: AuthResponse = {
      accessToken: 'mock-token-' + Date.now(),
      refreshToken: 'mock-refresh-' + Date.now(),
      user: mockUser,
    };
    return of(mockRes).pipe(tap((res) => this.setSession(res)));
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

  logout(): void {
    this.accessToken = null;
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  refresh(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(ApiEndpoints.auth.refresh, {}).pipe(
      tap((res) => this.setSession(res)),
    );
  }

  getToken(): string | null {
    return this.accessToken;
  }

  isAuthenticated(): boolean {
    return !!this.accessToken;
  }

  private setSession(res: AuthResponse): void {
    this.accessToken = res.accessToken;
    this.currentUser.set(res.user);
  }
}
