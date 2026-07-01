import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ShippingFormComponent } from '../components/shipping-form.component';
import { OrderReviewComponent } from '../components/order-review.component';
import { CheckoutService } from '@core/services/checkout.service';
import { CartStore } from '@features/cart/store/cart.store';
import { CheckoutRequest, ShippingAddress } from '@core/models/common.model';
import { switchMap, finalize } from 'rxjs';

@Component({
  selector: 'app-checkout',
  imports: [
    NzStepsModule,
    NzButtonModule,
    CurrencyFormatPipe,
    ShippingFormComponent,
    OrderReviewComponent,
  ],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent {
  private checkoutService = inject(CheckoutService);
  private store = inject(CartStore);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  currentStep = signal(0);
  shippingData = signal<ShippingAddress | null>(null);
  isPlacing = signal(false);

  selectedItems = this.store.selectedItems;
  selectedAmount = this.store.selectedAmount;

  constructor() {
    if (this.selectedItems().length === 0) {
      this.router.navigate(['/cart']);
    }
  }

  onShippingComplete(data: ShippingAddress) {
    this.shippingData.set(data);
    this.currentStep.set(1);
  }

  backToShipping() {
    this.currentStep.set(0);
  }

  placeOrder() {
    const addr = this.shippingData();
    if (!addr) return;

    this.isPlacing.set(true);
    const req: CheckoutRequest = {
      idempotencyKey: crypto.randomUUID(),
      fulfillmentType: 'DELIVERY',
      shippingAddress: addr,
    };

    this.checkoutService
      .checkout(req)
      .pipe(
        switchMap((res) => this.checkoutService.initiatePayment(res.id)),
        finalize(() => this.isPlacing.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((res) => {
        window.location.href = res.paymentUrl;
      });
  }
}
