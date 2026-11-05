import { Component, inject, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Eye, EyeOff, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { ToastService } from '@core/services/toast.service';
import { AuthService } from '@core/auth/auth.service';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { SKIP_AUTH_LOGOUT } from '@core/interceptors/auth.interceptor';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { clearGuestToken } from '@core/utils/guest-token';
import { catchError, finalize, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './login-form.component.html',
})
export class LoginFormComponent {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  readonly EyeIcon = Eye;
  readonly EyeOffIcon = EyeOff;
  readonly LoaderIcon = Loader2;

  loginSuccess = output();
  isLoading = signal(false);
  passwordVisible = signal(false);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  socialLogin(provider: string) {
    this.toast.info(`Đăng nhập ${provider} đang phát triển, bạn đăng nhập bằng email nhé`);
  }

  submit() {
    if (this.form.invalid) return;
    this.isLoading.set(true);
    this.authService
      .login(this.form.getRawValue())
      .pipe(
        switchMap(() => {
          let token: string | null = null;
          try {
            token = localStorage.getItem('pc-guest-token');
          } catch {
            token = null;
          }
          if (!token) return of(null);
          const params = new HttpParams().set('token', token);
          return this.http
            .post(
              ApiEndpoints.cart.merge,
              {},
              {
                params,
                context: new HttpContext().set(SKIP_AUTH_LOGOUT, true),
              },
            )
            .pipe(catchError(() => of(null)));
        }),
        finalize(() => this.isLoading.set(false)),
      )
      .subscribe(() => {
        clearGuestToken();
        this.loginSuccess.emit();
      });
  }
}
