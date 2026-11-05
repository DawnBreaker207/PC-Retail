import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { LoginFormComponent } from '../components/login-form.component';
import { RegisterFormComponent } from '../components/register-form.component';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-auth-page',
  imports: [RouterLink, LoginFormComponent, RegisterFormComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './auth-page.component.html',
})
export class AuthPageComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoginMode = signal(true);

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      // Already logged in (e.g. bounced here by SSR): keep returnUrl instead of dropping it.
      const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
      this.router.navigateByUrl(returnUrl);
    }
  }

  onLoginSuccess() {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    this.router.navigateByUrl(returnUrl);
  }

  onRegisterSuccess() {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    this.router.navigateByUrl(returnUrl);
  }
}
