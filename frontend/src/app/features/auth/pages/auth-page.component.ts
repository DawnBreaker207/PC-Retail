import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { LoginFormComponent } from '../components/login-form.component';
import { RegisterFormComponent } from '../components/register-form.component';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-auth-page',
  imports: [RouterLink, NzButtonModule, LoginFormComponent, RegisterFormComponent],
  templateUrl: './auth-page.component.html',
})
export class AuthPageComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoginMode = signal(true);

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
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
