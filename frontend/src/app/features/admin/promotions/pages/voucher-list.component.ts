import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { VoucherFormComponent } from '../components/voucher-form.component';

interface Voucher {
  id: number;
  code: string;
  scopeType: 'STOREWIDE' | 'CATEGORY' | 'PRODUCT' | 'FREESHIP';
  discountType: 'FIXED' | 'PERCENT';
  discountValue: number;
  maxDiscountAmount?: number;
  minOrderValue: number;
  totalQuantity: number;
  remainingQuantity: number;
  validFrom: string;
  validTo: string;
}

interface VoucherResponse {
  content: Voucher[];
  totalElements: number;
}

const SCOPE_LABEL: Record<string, string> = {
  STOREWIDE: 'Toàn cửa hàng',
  CATEGORY: 'Danh mục',
  PRODUCT: 'Sản phẩm',
  FREESHIP: 'Miễn phí ship',
};

const DISCOUNT_LABEL: Record<string, string> = {
  FIXED: 'Tiền mặt',
  PERCENT: 'Phần trăm',
};

@Component({
  selector: 'app-voucher-list',
  imports: [
    DatePipe,
    NzTableModule, NzButtonModule, NzIconModule, NzSpinModule, NzTagModule, NzPopconfirmModule, NzModalModule,
  ],
  templateUrl: './voucher-list.component.html',
})
export class VoucherListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);

  vouchers = signal<Voucher[]>([]);
  loading = signal(false);
  total = signal(0);
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  protected readonly SCOPE_LABEL = SCOPE_LABEL;
  protected readonly DISCOUNT_LABEL = DISCOUNT_LABEL;

  constructor() {
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    const params = new HttpParams()
      .set('page', this.page - 1)
      .set('size', this.size);

    this.http.get<VoucherResponse>(ApiEndpoints.promotion.vouchers, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.vouchers.set(res.content);
          this.total.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  getStatus(v: Voucher): { label: string; color: string } {
    const now = Date.now();
    const from = new Date(v.validFrom).getTime();
    const to = new Date(v.validTo).getTime();
    if (now < from) return { label: 'Sắp diễn ra', color: 'warning' };
    if (now > to || v.remainingQuantity <= 0) return { label: 'Hết hạn', color: 'default' };
    return { label: 'Đang hoạt động', color: 'success' };
  }

  getUsage(v: Voucher): string {
    const used = v.totalQuantity - v.remainingQuantity;
    return `${used}/${v.totalQuantity}`;
  }

  openCreateModal() {
    const modalRef = this.modal.create({
      nzTitle: 'Tạo voucher mới',
      nzContent: VoucherFormComponent,
      nzFooter: null,
      nzWidth: 600,
    });

    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success('Tạo voucher thành công');
        this.page = 1;
        this.loadData();
      }
    });
  }

  deleteVoucher(id: number) {
    this.http.delete(ApiEndpoints.promotion.voucherDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Xóa voucher thành công');
          this.loadData();
        },
        error: () => this.message.error('Xóa voucher thất bại'),
      });
  }

  onPageChange(page: number) {
    this.page = page;
    this.loadData();
  }

  onPageSizeChange(size: number) {
    this.size = size;
    this.page = 1;
    this.loadData();
  }

  formatDiscount(v: Voucher): string {
    if (v.discountType === 'FIXED') {
      return v.discountValue.toLocaleString('vi-VN') + '₫';
    }
    return v.discountValue + '%' + (v.maxDiscountAmount ? ` (tối đa ${v.maxDiscountAmount.toLocaleString('vi-VN')}₫)` : '');
  }
}
