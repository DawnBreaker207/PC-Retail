import {
  Component,
  inject,
  DestroyRef,
  signal,
  model,
  ChangeDetectionStrategy,
} from '@angular/core';
import { Location, NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  ArrowLeft,
  User,
  Phone,
  MapPin,
  Package,
  Truck,
  CheckCircle,
  CreditCard,
  AlertCircle,
  Clock,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
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
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { StatusFormatPipe } from '@shared/pipes/status-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Order } from '@core/models/common.model';

const STATUS_STEP_MAP: Record<string, number> = {
  PENDING_PAYMENT: 0,
  PAID: 1,
  PROCESSING: 2,
  SHIPPED: 3,
  DELIVERED: 4,
};

@Component({
  selector: 'app-admin-order-detail',
  standalone: true,
  imports: [
    NgClass,
    RouterLink,
    FormsModule,
    FallbackImageDirective,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
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
    CurrencyFormatPipe,
    StatusFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-order-detail.component.html',
})
export class AdminOrderDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);
  private location = inject(Location);

  readonly ArrowLeft = ArrowLeft;
  readonly User = User;
  readonly Phone = Phone;
  readonly MapPin = MapPin;
  readonly Package = Package;
  readonly Truck = Truck;
  readonly CheckCircle = CheckCircle;
  readonly CreditCard = CreditCard;
  readonly AlertCircle = AlertCircle;
  readonly Clock = Clock;
  readonly Loader2 = Loader2;

  order = signal<Order | null>(null);
  loading = signal(false);
  trackingCode = model('');
  updating = signal(false);

  // Spartan Confirmation Dialog State
  confirmModal = signal<{
    open: boolean;
    title: string;
    description: string;
    confirmText: string;
    action: () => void;
  } | null>(null);

  protected readonly STATUS_STEP_MAP = STATUS_STEP_MAP;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadOrder(id);
  }

  private loadOrder(id: number) {
    this.loading.set(true);
    this.http
      .get<Order>(ApiEndpoints.staffOrders.detail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.order.set(res);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  get currentStep(): number {
    const status = this.order()?.subOrders?.[0]?.status;
    return STATUS_STEP_MAP[status ?? ''] ?? 0;
  }

  updateStatus(status: string) {
    const o = this.order();
    if (!o) return;
    const subOrderId = o.subOrders?.[0]?.id ?? o.id;

    let confirmTitle = 'Xác nhận thay đổi trạng thái?';
    let confirmContent = `Đơn hàng #${o.id} sẽ được cập nhật trạng thái.`;
    if (status === 'PROCESSING') {
      confirmTitle = 'Xác nhận đơn hàng?';
      confirmContent = `Đơn #${o.id} sẽ chuyển sang trạng thái Đang xử lý.`;
    } else if (status === 'SHIPPED') {
      confirmTitle = 'Đánh dấu đã giao shipper?';
      confirmContent = `Xác nhận giao hàng cho đơn #${o.id}${this.trackingCode() ? ' với mã vận đơn ' + this.trackingCode() : ''}.`;
    } else if (status === 'DELIVERED') {
      confirmTitle = 'Xác nhận đã giao hàng thành công?';
      confirmContent = `Đơn #${o.id} sẽ được chuyển sang trạng thái Đã giao.`;
    } else if (status === 'CANCELLED') {
      confirmTitle = 'Hủy đơn hàng?';
      confirmContent = `Đơn #${o.id} sẽ bị hủy. Thao tác này không thể hoàn tác.`;
    }

    this.confirmModal.set({
      open: true,
      title: confirmTitle,
      description: confirmContent,
      confirmText: 'Xác nhận',
      action: () => {
        this.updating.set(true);
        const body: Record<string, unknown> = { status };
        if (status === 'SHIPPED' && this.trackingCode()) {
          body['trackingCode'] = this.trackingCode();
        }
        this.http
          .post(ApiEndpoints.staffOrders.transition(subOrderId), body)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.toast.success('Cập nhật trạng thái thành công');
              this.closeConfirmModal();
              this.loadOrder(o.id);
              this.updating.set(false);
            },
            error: () => {
              this.toast.error('Cập nhật thất bại');
              this.updating.set(false);
            },
          });
      },
    });
  }

  closeConfirmModal() {
    this.confirmModal.set(null);
  }

  get flatItems() {
    return this.order()?.subOrders?.flatMap((s) => s.items) ?? [];
  }

  goBack() {
    this.location.back();
  }
}
