import { Component, inject, signal, DestroyRef, OnDestroy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ShippingFormComponent } from '../components/shipping-form.component';
import { OrderReviewComponent } from '../components/order-review.component';
import { CheckoutService } from '@core/services/checkout.service';
import { AuthService } from '@core/auth/auth.service';
import { getGuestToken } from '@core/utils/guest-token';
import { CartStore } from '@features/cart/store/cart.store';
import { CheckoutRequest, ShippingAddress } from '@core/models/common.model';
import { switchMap, finalize } from 'rxjs';

@Component({
  selector: 'app-checkout',
  imports: [
    NzStepsModule,
    NzButtonModule,
    CurrencyFormatPipe, FallbackImageDirective,
    ShippingFormComponent,
    OrderReviewComponent,
  ],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnDestroy {
  private checkoutService = inject(CheckoutService);
  private auth = inject(AuthService);
  private store = inject(CartStore);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  currentStep = signal(0);
  shippingData = signal<ShippingAddress | null>(null);
  isPlacing = signal(false);
  windowWidth = signal(0);
  private createdOrderId = 0;
  private onResize = () => this.windowWidth.set(window.innerWidth);

  constructor() {
    if (typeof window !== 'undefined') {
      this.windowWidth.set(window.innerWidth);
      window.addEventListener('resize', this.onResize);
    }
    if (this.selectedItems().length === 0) {
      this.router?.navigate(['/cart']);
    }
  }

  ngOnDestroy() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', this.onResize);
    }
  }

  selectedItems = this.store.selectedItems;
  selectedAmount = this.store.selectedAmount;

  onShippingComplete(data: ShippingAddress) {
    this.shippingData.set(data);
    this.currentStep.set(1);
  }

  backToShipping() {
    this.currentStep.set(0);
  }

  placeOrder() {
    const addr = this.shippingData();
    if (!addr || this.isPlacing()) return;

    this.isPlacing.set(true);
    if (!this.auth.isAuthenticated()) {
      this.placeGuestOrder(addr);
      return;
    }
    const req: CheckoutRequest = {
      idempotencyKey: crypto.randomUUID(),
      fulfillmentType: 'DELIVERY',
      shippingAddress: addr,
    };

    this.checkoutService
      .checkout(req)
      .pipe(
        switchMap((res) => {
          this.createdOrderId = res.id;
          return this.checkoutService.initiatePayment(res.id);
        }),
        finalize(() => this.isPlacing.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          const ok = window.confirm('Mô phỏng thanh toán VNPay\n\nOK = Thành công\nHuỷ = Thất bại');
          const code = ok ? '00' : '01';
          window.location.href = `/checkout/result?orderId=${this.createdOrderId}&vnp_ResponseCode=${code}&vnp_TransactionNo=MOCK${Date.now()}`;
        },
        error: (err) => {
          console.error('Checkout/initiate failed:', err);
          window.alert('Lỗi: không thể tạo đơn hàng. Vui lòng thử lại.');
        },
      });
  }

  private placeGuestOrder(addr: ShippingAddress) {
    this.checkoutService
      .guestCheckout({
        idempotencyKey: crypto.randomUUID(),
        fulfillmentType: 'DELIVERY',
        shippingAddress: addr,
        guestToken: getGuestToken(),
        guestPhone: addr.phone,
      })
      .pipe(
        finalize(() => this.isPlacing.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (res) => {
          this.store.clearCart();
          this.router?.navigate(['/checkout/result'], {
            queryParams: { orderId: res.id, guest: 1 },
          });
        },
        error: () => {
          window.alert('Lỗi: không thể tạo đơn hàng. Vui lòng thử lại.');
        },
      });
  }
}
