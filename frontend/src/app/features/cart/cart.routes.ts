import { Routes } from '@angular/router';
import { CartComponent } from './pages/cart.component';

export const CART_ROUTES: Routes = [
  // Guest xem giỏ được (store tự rẽ nhánh theo login)
  { path: '', component: CartComponent },
];
