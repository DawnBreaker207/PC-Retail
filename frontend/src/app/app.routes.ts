import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout').then((m) => m.MainLayout),
    children: [
      {
        path: '',
        loadChildren: () => import('./features/home/home.routes').then((r) => r.HOME_ROUTES),
      },
      { path: 'home', redirectTo: '/', pathMatch: 'full' },
      {
        path: 'auth',
        loadChildren: () => import('./features/auth/auth.routes').then((r) => r.AUTH_ROUTES),
      },
      {
        path: 'cart',
        loadChildren: () => import('./features/cart/cart.routes').then((r) => r.CART_ROUTES),
      },
      {
        path: 'checkout',
        loadChildren: () => import('./features/checkout/checkout.routes').then((r) => r.CHECKOUT_ROUTES),
      },
      {
        path: 'orders',
        loadChildren: () => import('./features/orders/orders.routes').then((r) => r.ORDER_ROUTES),
      },
      {
        path: 'products',
        loadChildren: () => import('./features/catalog/catalog.routes').then((r) => r.CATALOG_ROUTES),
      },
      {
        path: 'error',
        loadChildren: () => import('./features/error/error.routes').then((r) => r.ERROR_ROUTES),
      },
    ],
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then((r) => r.ADMIN_ROUTES),
  },
  { path: '**', redirectTo: '/error/404' },
];
