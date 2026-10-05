import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-forgot-password-form',
  imports: [RouterLink, ReactiveFormsModule, NzFormModule, NzInputModule, NzButtonModule],
  templateUrl: './forgot-password-form.component.html',
})
export class ForgotPasswordFormComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  isLoading = signal(false);
  submitted = signal(false);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  submit() {
    if (this.form.invalid) return;
    this.isLoading.set(true);
    this.authService
      .forgotPassword(this.form.getRawValue().email)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(() => this.submitted.set(true));
  }
}
