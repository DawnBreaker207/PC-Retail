import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  DragDropModule,
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import {
  LucideAngularModule,
  Table as TableIcon,
  LayoutGrid,
  Search,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Loader2,
  AlertCircle,
  Download,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
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
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { StatusFormatPipe } from '@shared/pipes/status-format.pipe';
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
  totalElements?: number;
  pagination?: { totalElements: number };
}

interface KanbanCol {
  id: string;
  status: string;
  title: string;
  dotClass: string;
  badgeClass: string;
  orders: AdminOrder[];
}

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { value: 'PAID', label: 'Đã thanh toán' },
  { value: 'PROCESSING', label: 'Đang xử lý' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
  { value: 'RETURN_REQUESTED', label: 'Yêu cầu đổi trả' },
];

@Component({
  selector: 'app-admin-order-list',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    RouterLink,
    FormsModule,
    DragDropModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTooltipDirective,
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
    CurrencyFormatPipe,
    StatusFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-order-list.component.html',
})
export class AdminOrderListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly TableIcon = TableIcon;
  readonly LayoutGrid = LayoutGrid;
  readonly Search = Search;
  readonly CheckCircle2 = CheckCircle2;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly ExternalLink = ExternalLink;
  readonly Loader2 = Loader2;
  readonly AlertCircle = AlertCircle;
  readonly DownloadIcon = Download;

  viewMode = signal<'table' | 'kanban'>('table');
  orders = signal<AdminOrder[]>([]);
  loading = signal(false);
  exportingExcel = signal(false);
  total = signal(0);
  search = signal('');
  statusFilter = signal('');
  selectedIds = signal<Set<number>>(new Set());
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  // Modal confirm state
  confirmModal = signal<{
    open: boolean;
    title: string;
    description: string;
    confirmText: string;
    loading?: boolean;
    action: () => void;
  } | null>(null);

  kanbanColumns: KanbanCol[] = [
    {
      id: 'kb-pending',
      status: 'PENDING_PAYMENT',
      title: 'Chờ thanh toán',
      dotClass: 'bg-amber-500',
      badgeClass: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400',
      orders: [],
    },
    {
      id: 'kb-paid',
      status: 'PAID',
      title: 'Đã thanh toán',
      dotClass: 'bg-cyan-500',
      badgeClass: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/30 dark:text-cyan-400',
      orders: [],
    },
    {
      id: 'kb-processing',
      status: 'PROCESSING',
      title: 'Đang đóng gói',
      dotClass: 'bg-blue-500',
      badgeClass: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
      orders: [],
    },
    {
      id: 'kb-shipped',
      status: 'SHIPPED',
      title: 'Đang giao hàng',
      dotClass: 'bg-purple-500',
      badgeClass: 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400',
      orders: [],
    },
    {
      id: 'kb-delivered',
      status: 'DELIVERED',
      title: 'Đã giao thành công',
      dotClass: 'bg-emerald-500',
      badgeClass: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400',
      orders: [],
    },
  ];

  readonly kanbanConnectedIds = this.kanbanColumns.map((c) => c.id);
  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;

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

  private syncKanbanColumns(allOrders: AdminOrder[]) {
    this.kanbanColumns.forEach((col) => {
      col.orders = allOrders.filter((o) => {
        const orderStatus = o.subOrders?.[0]?.status || o.paymentStatus || 'PENDING_PAYMENT';
        return orderStatus === col.status;
      });
    });
  }

  onOrderDrop(event: CdkDragDrop<AdminOrder[]>, targetStatus: string): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const movedOrder = event.previousContainer.data[event.previousIndex];
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      const subOrderId = movedOrder.subOrders?.[0]?.id || movedOrder.id;
      const targetLabel =
        STATUS_OPTIONS.find((s) => s.value === targetStatus)?.label || targetStatus;

      this.toast.success(`Kéo đơn #${movedOrder.id} thành công: chuyển sang "${targetLabel}"`);

      // Update in-memory status
      if (movedOrder.subOrders?.[0]) {
        movedOrder.subOrders[0].status = targetStatus;
      }

      // Call staff transition API if applicable
      this.http
        .post(ApiEndpoints.staffOrders.transition(subOrderId), { status: targetStatus })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          error: () => {
            this.toast.error('Cập nhật trạng thái đơn hàng trên hệ thống thất bại');
          },
        });
    }
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams().set('page', this.page - 1).set('size', this.size);
    if (this.search()) params = params.set('search', this.search());
    if (this.statusFilter()) params = params.set('status', this.statusFilter());

    this.http
      .get<AdminOrderResponse>(ApiEndpoints.admin.orders, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.content ?? [];
          this.orders.set(list);
          this.syncKanbanColumns(list);
          this.total.set(res.totalElements ?? res.pagination?.totalElements ?? 0);
          this.selectedIds.set(new Set());
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
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
    this.confirmModal.set({
      open: true,
      title: 'Xác nhận đơn hàng hàng loạt?',
      description: `Bạn có chắc muốn xác nhận ${ids.length} đơn hàng đã chọn sang trạng thái Đang xử lý?`,
      confirmText: `Xác nhận tất cả (${ids.length})`,
      action: () => {
        let completed = 0;
        ids.forEach((id) => {
          this.http
            .post(ApiEndpoints.staffOrders.transition(id), { status: 'PROCESSING' })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                completed++;
                if (completed === ids.length) {
                  this.toast.success(`Đã xác nhận ${ids.length} đơn hàng thành công`);
                  this.selectedIds.set(new Set());
                  this.closeConfirmModal();
                  this.loadData();
                }
              },
              error: () => this.toast.error(`Xác nhận đơn #${id} thất bại`),
            });
        });
      },
    });
  }

  confirmOrder(id: number) {
    this.confirmModal.set({
      open: true,
      title: 'Xác nhận đơn hàng?',
      description: `Đơn #${id} sẽ chuyển sang trạng thái Đang xử lý.`,
      confirmText: 'Xác nhận xử lý',
      action: () => {
        this.http
          .post(ApiEndpoints.staffOrders.transition(id), { status: 'PROCESSING' })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.toast.success('Đã xác nhận đơn hàng thành công');
              this.closeConfirmModal();
              this.loadData();
            },
            error: () => this.toast.error('Xác nhận thất bại'),
          });
      },
    });
  }

  closeConfirmModal() {
    this.confirmModal.set(null);
  }

  exportOrdersExcel() {
    this.exportingExcel.set(true);
    this.http
      .get(ApiEndpoints.orders.exportExcel, { responseType: 'blob' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          this.exportingExcel.set(false);
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `orders_export_${Date.now()}.xlsx`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.toast.success('Xuất file Excel danh sách đơn hàng thành công!');
        },
        error: (err) => {
          this.exportingExcel.set(false);
          console.error('Export orders excel error:', err);
          this.toast.error('Không thể xuất file Excel đơn hàng lúc này');
        },
      });
  }

  getProductSummary(subOrders: AdminOrder['subOrders']): string {
    return (
      subOrders
        .flatMap((s) => s.items)
        .slice(0, 3)
        .map((i) => i.name)
        .join(', ') + (subOrders.flatMap((s) => s.items).length > 3 ? '...' : '')
    );
  }
}
