import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Voucher, VoucherValidationResult } from '@core/models/common.model';

@Component({
  selector: 'app-my-vouchers',
  imports: [FormsModule, NzInputModule, NzButtonModule, NzIconModule, NzSpinModule, CurrencyFormatPipe, EmptyStateComponent],
  templateUrl: './my-vouchers.component.html',
})
export class MyVouchersComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  vouchers = signal<Voucher[]>([]);
  loading = signal(true);
  code = '';
  checking = signal(false);

  constructor() {
    this.http.get<{ content: Voucher[] }>(ApiEndpoints.promotion.vouchers)
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
    if (!this.code.trim()) return;
    this.checking.set(true);
    this.http.post<VoucherValidationResult>(ApiEndpoints.promotion.validateVoucher, { code: this.code.trim() })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.checking.set(false);
          if (res.valid) {
            this.message.success(`Mã "${res.code}" hợp lệ — áp dụng khi thanh toán`);
          } else {
            this.message.error(res.message || 'Mã giảm giá không hợp lệ');
          }
        },
        error: () => {
          this.checking.set(false);
          this.message.error('Không kiểm tra được mã, thử lại sau');
        },
      });
  }
}
