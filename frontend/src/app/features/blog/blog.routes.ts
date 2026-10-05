import { Routes } from '@angular/router';

export const BLOG_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./pages/blog-list.component').then((m) => m.BlogListComponent) },
  { path: ':slug', loadComponent: () => import('./pages/blog-detail.component').then((m) => m.BlogDetailComponent) },
];
