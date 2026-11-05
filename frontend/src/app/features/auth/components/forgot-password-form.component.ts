import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { LucideAngularModule, Loader2, CheckCircle2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-forgot-password-form',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './forgot-password-form.component.html',
})
export class ForgotPasswordFormComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  readonly LoaderIcon = Loader2;
  readonly CheckCircleIcon = CheckCircle2;

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
