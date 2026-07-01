import { Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { AuthService } from '@core/auth/auth.service';
import { finalize, switchMap } from 'rxjs';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pwd = control.get('password');
  const confirm = control.get('confirmPassword');
  return pwd?.value !== confirm?.value ? { mismatch: true } : null;
}

@Component({
  selector: 'app-register-form',
  imports: [ReactiveFormsModule, NzFormModule, NzInputModule, NzButtonModule, NzIconModule],
  templateUrl: './register-form.component.html',
})
export class RegisterFormComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  registerSuccess = output();
  isLoading = signal(false);
  passwordVisible = signal(false);
  confirmVisible = signal(false);

  form = this.fb.nonNullable.group(
    {
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^(0[3|5|7|8|9])+([0-9]{8})$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator },
  );

  submit() {
    if (this.form.invalid) return;

    this.isLoading.set(true);
    const raw = this.form.getRawValue();
    const data = { fullName: raw.fullName, email: raw.email, phone: raw.phone, password: raw.password };

    this.authService
      .register(data)
      .pipe(
        switchMap(() =>
          this.authService.login({ email: data.email, password: data.password }),
        ),
        finalize(() => this.isLoading.set(false)),
      )
      .subscribe(() => this.registerSuccess.emit());
  }
}
