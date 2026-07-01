import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { roleGuard } from '@core/guards/role.guard';
import { AdminLayout } from '@layout/admin-layout/admin-layout';
import { DashboardComponent } from './pages/dashboard.component';
import { AdminPlaceholderComponent } from './components/admin-placeholder.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN', 'STAFF'] },
    children: [
      { path: '', component: DashboardComponent },
      { path: 'orders', component: AdminPlaceholderComponent },
      { path: 'products', component: AdminPlaceholderComponent },
      { path: 'inventory', component: AdminPlaceholderComponent },
      { path: 'categories', component: AdminPlaceholderComponent },
      { path: 'vouchers', component: AdminPlaceholderComponent },
      { path: 'users', component: AdminPlaceholderComponent },
    ],
  },
];
