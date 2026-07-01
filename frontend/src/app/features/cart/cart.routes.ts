import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { CartComponent } from './pages/cart.component';

export const CART_ROUTES: Routes = [
  { path: '', component: CartComponent, canActivate: [authGuard] },
];
