import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForgotPasswordFormComponent } from '../components/forgot-password-form.component';

@Component({
  selector: 'app-forgot-password-page',
  imports: [RouterLink, ForgotPasswordFormComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './forgot-password-page.component.html',
})
export class ForgotPasswordPageComponent {}
