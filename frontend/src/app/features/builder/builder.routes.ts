import { Routes } from '@angular/router';

export const BUILDER_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', redirectTo: 'custom', pathMatch: 'full' },
      {
        path: 'custom',
        loadComponent: () => import('./pages/custom-build.component').then(m => m.CustomBuildComponent),
      },
      {
        path: 'templates',
        loadComponent: () => import('./pages/template-list.component').then(m => m.TemplateListComponent),
      },
      {
        path: 'templates/:id',
        loadComponent: () => import('./pages/template-detail.component').then(m => m.TemplateDetailComponent),
      },
    ],
  },
];
