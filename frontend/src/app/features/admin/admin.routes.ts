import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { roleGuard } from '@core/guards/role.guard';
import { AdminLayout } from '@layout/admin-layout/admin-layout';
import { DashboardComponent } from './pages/dashboard.component';
import { AdminOrderListComponent } from './orders/pages/admin-order-list.component';
import { AdminOrderDetailComponent } from './orders/pages/admin-order-detail.component';
import { InventoryListComponent } from './inventory/pages/inventory-list.component';
import { VoucherListComponent } from './promotions/pages/voucher-list.component';

const STAFF_AND_ADMIN = ['ADMIN', 'STAFF'];
const ADMIN_ONLY = ['ADMIN'];

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayout,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardComponent, canActivate: [roleGuard], data: { roles: STAFF_AND_ADMIN } },
      {
        path: 'account',
        canActivate: [roleGuard],
        data: { roles: STAFF_AND_ADMIN },
        loadComponent: () => import('../account/pages/account.component').then(m => m.AccountComponent),
      },
      { path: 'orders', component: AdminOrderListComponent, canActivate: [roleGuard], data: { roles: STAFF_AND_ADMIN } },
      { path: 'orders/:id', component: AdminOrderDetailComponent, canActivate: [roleGuard], data: { roles: STAFF_AND_ADMIN } },
      {
        path: 'returns',
        canActivate: [roleGuard],
        data: { roles: STAFF_AND_ADMIN },
        loadComponent: () => import('./returns/pages/admin-return-list.component').then(m => m.AdminReturnListComponent),
      },
      {
        path: 'returns/:id',
        canActivate: [roleGuard],
        data: { roles: STAFF_AND_ADMIN },
        loadComponent: () => import('./returns/pages/admin-return-detail.component').then(m => m.AdminReturnDetailComponent),
      },
      {
        path: 'products',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./products/pages/admin-product-list.component').then(m => m.AdminProductListComponent),
      },
      { path: 'inventory', component: InventoryListComponent, canActivate: [roleGuard], data: { roles: ADMIN_ONLY } },
      {
        path: 'categories',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./categories/pages/admin-category-list.component').then(m => m.AdminCategoryListComponent),
      },
      { path: 'vouchers', component: VoucherListComponent, canActivate: [roleGuard], data: { roles: ADMIN_ONLY } },
      {
        path: 'reviews',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./reviews/pages/admin-review-list.component').then(m => m.AdminReviewListComponent),
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./users/pages/admin-user-list.component').then(m => m.AdminUserListComponent),
      },
      {
        path: 'audit-log',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./audit-log/pages/audit-log-list.component').then(m => m.AuditLogListComponent),
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./reports/pages/admin-reports.component').then(m => m.AdminReportsComponent),
      },
      {
        path: 'purchasing',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./purchasing/pages/admin-purchasing.component').then(m => m.AdminPurchasingComponent),
      },
      {
        path: 'stocktake',
        canActivate: [roleGuard],
        data: { roles: ADMIN_ONLY },
        loadComponent: () => import('./stocktake/pages/admin-stocktake.component').then(m => m.AdminStocktakeComponent),
      },
    ],
  },
];
