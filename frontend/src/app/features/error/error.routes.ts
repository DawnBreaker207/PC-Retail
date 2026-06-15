import { Routes } from '@angular/router';
import { ErrorLayoutComponent } from '../../layout/error-layout/error-layout.component';
import { Error404Component } from '@features/error/error/error-404.component';
import { Error500Component } from '@features/error/500/error-500.component';

export const ERROR_ROUTES: Routes = [
  {
    path: '',
    component: ErrorLayoutComponent,
    children: [
      { path: '', redirectTo: '404', pathMatch: 'full' },
      { path: '404', component: Error404Component },
      { path: '500', component: Error500Component },
    ],
  },
  { path: '**', redirectTo: '404' },
];
