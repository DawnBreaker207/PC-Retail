import { Component, DestroyRef, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Ticket, Gift, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Voucher, VoucherValidationResult } from '@core/models/common.model';

@Component({
  selector: 'app-my-vouchers',
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    CurrencyFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './my-vouchers.component.html',
})
export class MyVouchersComponent {
  readonly Ticket = Ticket;
  readonly Gift = Gift;
  readonly Loader2 = Loader2;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  vouchers = signal<Voucher[]>([]);
  loading = signal(true);
  code = signal('');
  checking = signal(false);

  constructor() {
    this.http
      .get<{ content: Voucher[] }>(ApiEndpoints.promotion.vouchers)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.vouchers.set(res.content);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  formatDiscount(v: Voucher): string {
    return v.type === 'FIXED' ? v.discount.toLocaleString('vi-VN') + '₫' : v.discount + '%';
  }

  checkCode() {
    const val = this.code().trim();
    if (!val) return;
    this.checking.set(true);
    this.http
      .post<VoucherValidationResult>(ApiEndpoints.promotion.validateVoucher, { code: val })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.checking.set(false);
          if (res.valid) {
            this.toast.success(`Mã "${res.code}" hợp lệ — có thể áp dụng khi thanh toán`);
          } else {
            this.toast.error(res.message || 'Mã giảm giá không hợp lệ');
          }
        },
        error: () => {
          this.checking.set(false);
          this.toast.error('Không kiểm tra được mã, vui lòng thử lại sau');
        },
      });
  }
}
