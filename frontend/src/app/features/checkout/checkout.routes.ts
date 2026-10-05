import { Routes } from '@angular/router';
import { CheckoutComponent } from './pages/checkout.component';
import { PaymentResultComponent } from './pages/payment-result.component';

export const CHECKOUT_ROUTES: Routes = [
  // Guest mua được không cần login (component tự rẽ nhánh member/guest)
  { path: '', component: CheckoutComponent },
  { path: 'result', component: PaymentResultComponent },
];
