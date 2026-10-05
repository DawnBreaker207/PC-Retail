import { Routes } from '@angular/router';
import { AuthPageComponent } from './pages/auth-page.component';

export const AUTH_ROUTES: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: AuthPageComponent },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/forgot-password-page.component').then((m) => m.ForgotPasswordPageComponent),
  },
];
