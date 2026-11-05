import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  LucideAngularModule,
  Plus,
  Trash2,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Loader2,
  Ticket,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import {
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { ToastService } from '@core/services/toast.service';
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
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    VoucherFormComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './voucher-list.component.html',
})
export class VoucherListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly Plus = Plus;
  readonly Trash2 = Trash2;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly AlertCircle = AlertCircle;
  readonly Loader2 = Loader2;
  readonly Ticket = Ticket;

  vouchers = signal<Voucher[]>([]);
  loading = signal(false);
  total = signal(0);
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  isCreateModalOpen = signal(false);
  deleteTarget = signal<Voucher | null>(null);

  protected readonly SCOPE_LABEL = SCOPE_LABEL;
  protected readonly DISCOUNT_LABEL = DISCOUNT_LABEL;

  constructor() {
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    const params = new HttpParams().set('page', this.page - 1).set('size', this.size);

    this.http
      .get<VoucherResponse>(ApiEndpoints.promotion.vouchers, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.vouchers.set(res.content ?? []);
          this.total.set(res.totalElements ?? 0);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  getStatus(v: Voucher): { label: string; badgeClass: string } {
    const now = Date.now();
    const from = new Date(v.validFrom).getTime();
    const to = new Date(v.validTo).getTime();
    if (now < from) {
      return {
        label: 'Sắp diễn ra',
        badgeClass:
          'border-amber-400 bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400',
      };
    }
    if (now > to || v.remainingQuantity <= 0) {
      return {
        label: 'Hết hạn',
        badgeClass:
          'border-slate-300 bg-slate-50 text-slate-700 dark:bg-slate-900/40 dark:text-slate-400',
      };
    }
    return {
      label: 'Đang hoạt động',
      badgeClass:
        'border-emerald-400 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400',
    };
  }

  getUsage(v: Voucher): string {
    const used = v.totalQuantity - v.remainingQuantity;
    return `${used}/${v.totalQuantity}`;
  }

  openCreateModal() {
    this.isCreateModalOpen.set(true);
  }

  closeCreateModal() {
    this.isCreateModalOpen.set(false);
  }

  onCreated() {
    this.closeCreateModal();
    this.page = 1;
    this.loadData();
  }

  confirmDelete(v: Voucher) {
    this.deleteTarget.set(v);
  }

  cancelDelete() {
    this.deleteTarget.set(null);
  }

  executeDelete() {
    const v = this.deleteTarget();
    if (!v) return;
    this.http
      .delete(ApiEndpoints.promotion.voucherDetail(v.id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Xóa voucher thành công');
          this.cancelDelete();
          this.loadData();
        },
        error: () => this.toast.error('Xóa voucher thất bại'),
      });
  }

  onPageChange(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.page = page;
    this.loadData();
  }

  onPageSizeChange(size: number) {
    this.size = size;
    this.page = 1;
    this.loadData();
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.total() / this.size));
  }

  formatDiscount(v: Voucher): string {
    if (v.discountType === 'FIXED') {
      return v.discountValue.toLocaleString('vi-VN') + '₫';
    }
    return (
      v.discountValue +
      '%' +
      (v.maxDiscountAmount ? ` (tối đa ${v.maxDiscountAmount.toLocaleString('vi-VN')}₫)` : '')
    );
  }
}
