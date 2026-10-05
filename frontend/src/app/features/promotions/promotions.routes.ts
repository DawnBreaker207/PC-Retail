import { Routes } from '@angular/router';

export const PROMOTIONS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./pages/promotions.component').then((m) => m.PromotionsComponent) },
];
