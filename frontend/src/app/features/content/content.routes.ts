import { Routes } from '@angular/router';

export const CONTENT_ROUTES: Routes = [
  { path: 'gioi-thieu', loadComponent: () => import('./pages/about.component').then((m) => m.AboutComponent) },
  { path: 'lien-he', loadComponent: () => import('./pages/contact.component').then((m) => m.ContactComponent) },
  { path: 'faq', loadComponent: () => import('./pages/faq.component').then((m) => m.FaqComponent) },
  { path: 'he-thong-showroom', loadComponent: () => import('./pages/showroom.component').then((m) => m.ShowroomComponent) },
  { path: 'chinh-sach/:slug', loadComponent: () => import('./pages/policy-page.component').then((m) => m.PolicyPageComponent) },
];
