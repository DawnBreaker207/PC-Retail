import { Routes } from '@angular/router';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: MainLayout },
  {
    path: 'error',
    loadChildren: () => import('./features/error/error.routes').then((r) => r.ERROR_ROUTES),
  },
  { path: '**', redirectTo: '404' },
];
