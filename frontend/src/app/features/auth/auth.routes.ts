import { Routes } from '@angular/router';
import { AuthPageComponent } from './pages/auth-page.component';

export const AUTH_ROUTES: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: AuthPageComponent },
];
