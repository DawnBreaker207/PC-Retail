import {
  Component,
  inject,
  OnInit,
  signal,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
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
  ShieldCheck,
  ReceiptText,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmSkeletonComponent } from '@shared/ui/skeleton';
import { HlmAlertDialogComponent } from '@shared/ui/alert-dialog';
import { HlmSeparatorDirective } from '@shared/ui/separator';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { DateHelper } from '@shared/utils/date.helper';
import { OrderService } from '@core/services/order.service';
import { Order, OrderItem, AdminReturnRequest } from '@core/models/common.model';
import { ReviewFormComponent } from '../components/review-form.component';
import { ReturnRequestModalComponent } from '../components/return-request-modal.component';
import { WarrantyClaimModalComponent } from '../components/warranty-claim-modal.component';

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
    HlmSkeletonComponent,
    HlmAlertDialogComponent,
    HlmSeparatorDirective,
    CurrencyFormatPipe,
    ReviewFormComponent,
    ReturnRequestModalComponent,
    WarrantyClaimModalComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
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
  readonly ShieldCheckIcon = ShieldCheck;
  readonly ReceiptTextIcon = ReceiptText;

  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private destroyRef = inject(DestroyRef);

  order = signal<Order | null>(null);
  isLoading = signal(false);
  returnInfo = signal<AdminReturnRequest | null>(null);
  invoiceInfo = signal<any>(null);
  requestingInvoice = signal(false);
  reviewItem = signal<OrderItem | null>(null);
  cancelDialogOpen = signal(false);
  returnModalOpen = signal(false);
  warrantyModalOpen = signal(false);

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
    this.orderService
      .getOrderDetail(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.order.set(res);
          this.isLoading.set(false);
          const subId = res?.subOrders[0]?.id;
          if (subId) this.loadReturn(subId);
          if (res?.id) this.loadInvoice(res.id);
        },
        error: () => this.isLoading.set(false),
      });
  }

  private loadInvoice(orderId: number) {
    this.orderService
      .getOrderInvoice(orderId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => of(null)),
      )
      .subscribe((inv) => this.invoiceInfo.set(inv));
  }

  requestInvoice() {
    const o = this.order();
    if (!o) return;
    this.requestingInvoice.set(true);
    this.orderService
      .requestOrderInvoice(o.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (inv) => {
          this.requestingInvoice.set(false);
          this.invoiceInfo.set(inv);
        },
        error: () => {
          this.requestingInvoice.set(false);
        },
      });
  }

  private loadReturn(subOrderId: number) {
    this.orderService
      .getReturnBySubOrder(subOrderId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => of(null)),
      )
      .subscribe((res) => this.returnInfo.set(res as AdminReturnRequest | null));
  }

  disputeReturn() {
    const r = this.returnInfo();
    const subId = this.order()?.subOrders[0]?.id;
    if (!r || !subId) return;
    const note = window.prompt('Lý do khiếu nại:');
    if (!note) return;
    this.orderService
      .disputeReturn(r.id, note)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadReturn(subId));
  }

  cancelOrder() {
    this.cancelDialogOpen.set(true);
  }

  confirmCancelOrder() {
    const o = this.order();
    if (!o) return;
    this.cancelDialogOpen.set(false);
    this.orderService
      .cancelOrder(o.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadOrder(o.id));
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
    this.returnModalOpen.set(true);
  }

  openWarrantyModal() {
    this.warrantyModalOpen.set(true);
  }

  onWarrantyClaimSubmitted() {
    this.warrantyModalOpen.set(false);
    const o = this.order();
    if (o) {
      this.loadOrder(o.id);
    }
  }

  onReturnSubmitted() {
    this.returnModalOpen.set(false);
    const o = this.order();
    const subId = o?.subOrders[0]?.id;
    if (o && subId) {
      this.loadOrder(o.id);
      this.loadReturn(subId);
    }
  }

  openReviewForm(item: OrderItem) {
    this.reviewItem.set(item);
  }

  protected readonly DateHelper = DateHelper;
}
