import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, Observable } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User, LoginRequest, AuthResponse, RegisterRequest } from '@core/models/common.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  currentUser = signal<User | null>(null);
  private accessToken: string | null = null;

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(ApiEndpoints.auth.login, credentials).pipe(
      tap((res) => this.setSession(res)),
    );
  }

  register(data: RegisterRequest): Observable<User> {
    return this.http.post<User>(ApiEndpoints.auth.register, data);
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
