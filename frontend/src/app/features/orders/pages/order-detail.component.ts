import { Component, inject, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  ChevronLeft,
  MapPin,
  AlertCircle,
  CheckCircle2,
  Clock,
  Truck,
  Package,
  XCircle,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { DateHelper } from '@shared/utils/date.helper';
import { OrderService } from '@core/services/order.service';
import { Order, OrderItem, AdminReturnRequest } from '@core/models/common.model';
import { ReviewFormComponent } from '../components/review-form.component';

const STATUS_STEPS = [
  { key: 'PENDING_PAYMENT', label: 'Đặt hàng' },
  { key: 'PAID', label: 'Đã thanh toán' },
  { key: 'PROCESSING', label: 'Đang xử lý' },
  { key: 'SHIPPED', label: 'Đang giao' },
  { key: 'DELIVERED', label: 'Đã giao' },
] as const;

const STATUS_STEP_MAP: Record<string, number> = {
  PENDING_PAYMENT: 0,
  PAID: 1,
  PROCESSING: 2,
  SHIPPED: 3,
  DELIVERED: 4,
};

@Component({
  selector: 'app-order-detail',
  imports: [
    RouterLink,
    FallbackImageDirective,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
    ReviewFormComponent,
  ],
  templateUrl: './order-detail.component.html',
})
export class OrderDetailComponent implements OnInit {
  readonly ChevronLeft = ChevronLeft;
  readonly MapPin = MapPin;
  readonly AlertCircle = AlertCircle;
  readonly CheckCircle2 = CheckCircle2;
  readonly Clock = Clock;
  readonly Truck = Truck;
  readonly Package = Package;
  readonly XCircle = XCircle;

  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private destroyRef = inject(DestroyRef);

  order = signal<Order | null>(null);
  isLoading = signal(false);
  returnInfo = signal<AdminReturnRequest | null>(null);
  reviewItem = signal<OrderItem | null>(null);

  steps = STATUS_STEPS;

  protected readonly RETURN_LABEL: Record<string, string> = {
    REQUESTED: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Đã từ chối',
    DISPUTED: 'Đang khiếu nại',
  };

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadOrder(id);
  }

  loadOrder(id: number) {
    this.isLoading.set(true);
    this.orderService.getOrderDetail(id).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (res) => {
        this.order.set(res);
        this.isLoading.set(false);
        const subId = res?.subOrders[0]?.id;
        if (subId) this.loadReturn(subId);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private loadReturn(subOrderId: number) {
    this.orderService.getReturnBySubOrder(subOrderId).pipe(
      takeUntilDestroyed(this.destroyRef),
      catchError(() => of(null)),
    ).subscribe((res) => this.returnInfo.set(res as AdminReturnRequest | null));
  }

  disputeReturn() {
    const r = this.returnInfo();
    const subId = this.order()?.subOrders[0]?.id;
    if (!r || !subId) return;
    const note = window.prompt('Lý do khiếu nại:');
    if (!note) return;
    this.orderService.disputeReturn(r.id, note).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.loadReturn(subId));
  }

  cancelOrder() {
    const o = this.order();
    if (!o) return;
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return;
    this.orderService.cancelOrder(o.id).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.loadOrder(o.id));
  }

  get currentStep(): number {
    const status = this.order()?.subOrders[0]?.status;
    return STATUS_STEP_MAP[status ?? ''] ?? 0;
  }

  get isCancellable(): boolean {
    const status = this.order()?.subOrders[0]?.status;
    return status === 'PENDING_PAYMENT' || status === 'PAID';
  }

  get isReturnable(): boolean {
    return this.order()?.subOrders[0]?.status === 'DELIVERED';
  }

  requestReturn() {
    const o = this.order();
    const subId = o?.subOrders[0]?.id;
    if (!o || !subId) return;
    const reason = window.prompt('Lý do yêu cầu đổi trả / hoàn tiền:');
    if (!reason) return;
    this.orderService.requestReturn(o.id, subId, reason).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.loadOrder(o.id);
      this.loadReturn(subId);
    });
  }

  openReviewForm(item: OrderItem) {
    this.reviewItem.set(item);
  }

  protected readonly DateHelper = DateHelper;
}

