import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface AdminOrder {
  id: number;
  userId: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  createdAt: string;
  totalAmount: number;
  paymentStatus: string;
  subOrders: { id: number; status: string; items: { name: string; quantity: number }[] }[];
}

interface AdminOrderResponse {
  content: AdminOrder[];
  totalElements: number;
}

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { value: 'PAID', label: 'Đã thanh toán' },
  { value: 'PROCESSING', label: 'Đang xử lý' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
  { value: 'RETURN_REQUESTED', label: 'Yêu cầu đổi trả' },
];

const STATUS_COLOR: Record<string, string> = {
  PENDING_PAYMENT: 'gold',
  PAID: 'blue',
  PROCESSING: 'processing',
  SHIPPED: 'purple',
  DELIVERED: 'success',
  CANCELLED: 'error',
  RETURN_REQUESTED: 'warning',
};

@Component({
  selector: 'app-admin-order-list',
  imports: [
    DatePipe, RouterLink, FormsModule,
    NzTableModule, NzInputModule, NzButtonModule, NzIconModule, NzSpinModule,
    NzTagModule, NzSelectModule, NzDatePickerModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './admin-order-list.component.html',
})
export class AdminOrderListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  orders = signal<AdminOrder[]>([]);
  loading = signal(false);
  total = signal(0);
  search = signal('');
  statusFilter = signal('');
  selectedIds = signal<Set<number>>(new Set());
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;
  protected readonly STATUS_COLOR = STATUS_COLOR;

  constructor() {
    this.loadData();
  }

  onSearch() {
    this.page = 1;
    this.loadData();
  }

  onStatusChange(status: string) {
    this.statusFilter.set(status);
    this.page = 1;
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams()
      .set('page', this.page - 1)
      .set('size', this.size);
    if (this.search()) params = params.set('search', this.search());
    if (this.statusFilter()) params = params.set('status', this.statusFilter());

    this.http.get<AdminOrderResponse>(ApiEndpoints.admin.orders, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.orders.set(res.content);
          this.total.set(res.totalElements);
          this.selectedIds.set(new Set());
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
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

  isAllSelected(): boolean {
    return this.orders().length > 0 && this.orders().every((o) => this.selectedIds().has(o.id));
  }

  toggleAll() {
    if (this.isAllSelected()) {
      this.selectedIds.set(new Set());
    } else {
      this.selectedIds.set(new Set(this.orders().map((o) => o.id)));
    }
  }

  toggleOne(id: number) {
    const set = new Set(this.selectedIds());
    if (set.has(id)) set.delete(id);
    else set.add(id);
    this.selectedIds.set(set);
  }

  get bulkSelectedIds(): number[] {
    return this.orders()
      .filter((o) => this.selectedIds().has(o.id) && o.subOrders[0]?.status === 'PAID')
      .map((o) => o.id);
  }

  get hasBulkConfirmable(): boolean {
    return this.bulkSelectedIds.length > 0;
  }

  bulkConfirm() {
    const ids = this.bulkSelectedIds;
    if (!ids.length) return;
    let completed = 0;
    ids.forEach((id) => {
      this.http.put(ApiEndpoints.admin.orderStatus(id), { status: 'PROCESSING' })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            completed++;
            if (completed === ids.length) {
              this.message.success(`Đã xác nhận ${ids.length} đơn hàng`);
              this.loadData();
            }
          },
        });
    });
  }

  confirmOrder(id: number) {
    this.http.put(ApiEndpoints.admin.orderStatus(id), { status: 'PROCESSING' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Đã xác nhận đơn hàng');
          this.loadData();
        },
      });
  }

  getProductSummary(subOrders: AdminOrder['subOrders']): string {
    return subOrders
      .flatMap((s) => s.items)
      .slice(0, 3)
      .map((i) => i.name)
      .join(', ') + (subOrders.flatMap((s) => s.items).length > 3 ? '...' : '');
  }
}
