import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';

export const ACCOUNT_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/account.component').then((m) => m.AccountComponent),
  },
];
