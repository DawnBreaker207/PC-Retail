import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { CheckoutComponent } from './pages/checkout.component';
import { PaymentResultComponent } from './pages/payment-result.component';

export const CHECKOUT_ROUTES: Routes = [
  { path: '', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'result', component: PaymentResultComponent },
];
