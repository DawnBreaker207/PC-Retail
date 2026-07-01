import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { OrderListComponent } from './pages/order-list.component';
import { OrderDetailComponent } from './pages/order-detail.component';

export const ORDER_ROUTES: Routes = [
  { path: '', component: OrderListComponent, canActivate: [authGuard] },
  { path: ':id', component: OrderDetailComponent, canActivate: [authGuard] },
];
