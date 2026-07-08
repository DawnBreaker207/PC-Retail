import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { roleGuard } from '@core/guards/role.guard';
import { AdminLayout } from '@layout/admin-layout/admin-layout';
import { DashboardComponent } from './pages/dashboard.component';
import { AdminOrderListComponent } from './orders/pages/admin-order-list.component';
import { AdminOrderDetailComponent } from './orders/pages/admin-order-detail.component';
import { InventoryListComponent } from './inventory/pages/inventory-list.component';
import { VoucherListComponent } from './promotions/pages/voucher-list.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN', 'STAFF'] },
    children: [
      { path: '', component: DashboardComponent },
      { path: 'orders', component: AdminOrderListComponent },
      { path: 'orders/:id', component: AdminOrderDetailComponent },
      { path: 'products', loadComponent: () => import('./components/admin-placeholder.component').then(m => m.AdminPlaceholderComponent) },
      { path: 'inventory', component: InventoryListComponent },
      { path: 'categories', loadComponent: () => import('./components/admin-placeholder.component').then(m => m.AdminPlaceholderComponent) },
      { path: 'vouchers', component: VoucherListComponent },
      { path: 'users', loadComponent: () => import('./components/admin-placeholder.component').then(m => m.AdminPlaceholderComponent) },
    ],
  },
];
