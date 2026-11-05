import {
  Component,
  effect,
  inject,
  signal,
  DestroyRef,
  PLATFORM_ID,
  OnDestroy,
  ChangeDetectionStrategy,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { LucideAngularModule, Check, Truck, ShieldCheck, CreditCard } from 'lucide-angular';
import { HlmSeparatorDirective } from '@shared/ui/separator';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ShippingFormComponent, ShippingStepResult } from '../components/shipping-form.component';
import { OrderReviewComponent } from '../components/order-review.component';
import { CheckoutService } from '@core/services/checkout.service';
import { AuthService } from '@core/auth/auth.service';
import { getGuestToken } from '@core/utils/guest-token';
import { CartStore } from '@features/cart/store/cart.store';
import { CheckoutRequest, ShippingAddress } from '@core/models/common.model';
import { switchMap, finalize } from 'rxjs';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    LucideAngularModule,
    CurrencyFormatPipe,
    FallbackImageDirective,
    ShippingFormComponent,
    OrderReviewComponent,
    HlmSeparatorDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnDestroy {
  private checkoutService = inject(CheckoutService);
  private auth = inject(AuthService);
  private store = inject(CartStore);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly CheckIcon = Check;
  readonly TruckIcon = Truck;
  readonly ShieldCheckIcon = ShieldCheck;
  readonly CreditCardIcon = CreditCard;

  currentStep = signal(0);
  shippingData = signal<ShippingStepResult | null>(null);
  isPlacing = signal(false);
  private createdOrderId = 0;
  private orderPlaced = false;

  constructor() {
    this.store.loadCart();
    effect(() => {
      if (!this.isBrowser) return;
      if (
        !this.orderPlaced &&
        !this.isPlacing() &&
        !this.store.isLoading() &&
        this.selectedItems().length === 0
      ) {
        this.router?.navigate(['/cart']);
      }
    });
  }

  ngOnDestroy() {
    // cleanup
  }

  selectedItems = this.store.selectedItems;
  selectedAmount = this.store.selectedAmount;

  onShippingComplete(data: ShippingStepResult) {
    this.shippingData.set(data);
    this.currentStep.set(1);
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  backToShipping() {
    this.currentStep.set(0);
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  placeOrder() {
    const sData = this.shippingData();
    if (!sData || this.isPlacing()) return;

    this.isPlacing.set(true);
    if (!this.auth.isAuthenticated()) {
      this.placeGuestOrder(sData);
      return;
    }
    const req: CheckoutRequest = {
      idempotencyKey: crypto.randomUUID(),
      fulfillmentType: sData.fulfillmentType,
      shippingAddress: sData.address,
      taxInfo: sData.taxInfo,
      invoiceRequested: sData.invoiceRequested,
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

  private placeGuestOrder(sData: ShippingStepResult) {
    this.isPlacing.set(true);
    this.checkoutService
      .guestCheckout({
        idempotencyKey: crypto.randomUUID(),
        fulfillmentType: sData.fulfillmentType,
        shippingAddress: sData.address,
        guestToken: getGuestToken(),
        guestPhone: sData.address.phone,
      })
      .pipe(
        finalize(() => this.isPlacing.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (res) => {
          this.orderPlaced = true;
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
