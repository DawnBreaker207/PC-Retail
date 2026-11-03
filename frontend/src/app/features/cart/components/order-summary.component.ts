import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Ticket, CheckCircle2, X } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartStore } from '@features/cart/store/cart.store';
import { PromotionService } from '@core/services/promotion.service';
import { ToastService } from '@core/services/toast.service';

@Component({
  selector: 'app-order-summary',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    CurrencyFormatPipe,
  ],
  templateUrl: './order-summary.component.html',
})
export class OrderSummaryComponent {
  readonly store = inject(CartStore);
  private promotionService = inject(PromotionService);
  private toast = inject(ToastService);

  readonly TicketIcon = Ticket;
  readonly CheckCircleIcon = CheckCircle2;
  readonly XIcon = X;

  voucherInput = signal('');
  appliedVoucher = signal<string | null>(null);
  voucherDiscount = signal(0);
  isValidating = signal(false);

  get totalAmount() {
    return this.store.totalAmount;
  }
  get selectedAmount() {
    return this.store.selectedAmount;
  }
  get selectedCount() {
    return this.store.selectedItems;
  }

  get finalTotal() {
    const raw = this.selectedAmount();
    const discount = this.voucherDiscount();
    return Math.max(0, raw - discount);
  }

  applyVoucher() {
    const code = this.voucherInput().trim().toUpperCase();
    if (!code) {
      this.toast.warning('Vui lòng nhập mã giảm giá');
      return;
    }
    const total = this.selectedAmount();
    if (total <= 0) {
      this.toast.warning('Vui lòng chọn ít nhất một sản phẩm trong giỏ hàng');
      return;
    }

    this.isValidating.set(true);
    this.promotionService.validateVoucher(code, total).subscribe({
      next: (res) => {
        this.isValidating.set(false);
        if (res.valid) {
          this.appliedVoucher.set(code);
          this.voucherDiscount.set(res.discountAmount || 0);
          this.toast.success(`Đã áp dụng mã giảm giá ${code} thành công!`);
        } else {
          this.toast.error(res.errorMessage || 'Mã giảm giá không hợp lệ hoặc không đủ điều kiện');
        }
      },
      error: () => {
        this.isValidating.set(false);
        this.toast.error('Không thể kiểm tra mã giảm giá lúc này');
      },
    });
  }

  removeVoucher() {
    this.appliedVoucher.set(null);
    this.voucherDiscount.set(0);
    this.voucherInput.set('');
    this.toast.info('Đã hủy áp dụng mã giảm giá');
  }
}
