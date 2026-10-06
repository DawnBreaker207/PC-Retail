import { Component, inject, OnInit, OnDestroy, signal, ViewContainerRef, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { DateHelper } from '@shared/utils/date.helper';
import { OrderService } from '@core/services/order.service';
import { Order, OrderItem, AdminReturnRequest } from '@core/models/common.model';
import { ReviewFormComponent } from '../components/review-form.component';

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
    RouterLink, FallbackImageDirective,
    NzStepsModule,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzAlertModule,
    NzSpinModule,
    NzModalModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './order-detail.component.html',
})
export class OrderDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private modal = inject(NzModalService);
  private viewContainerRef = inject(ViewContainerRef);
  private destroyRef = inject(DestroyRef);

  order = signal<Order | null>(null);
  isLoading = signal(false);
  returnInfo = signal<AdminReturnRequest | null>(null);

  protected readonly RETURN_LABEL: Record<string, string> = {
    REQUESTED: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Đã từ chối', DISPUTED: 'Đang khiếu nại',
  };
  protected readonly RETURN_COLOR: Record<string, string> = {
    REQUESTED: 'gold', APPROVED: 'success', REJECTED: 'error', DISPUTED: 'processing',
  };
  windowWidth = signal(typeof window !== 'undefined' ? window.innerWidth : 1280);
  private onResize = () => this.windowWidth.set(window.innerWidth);

  constructor() {
    if (typeof window !== 'undefined') {
      window.addEventListener('resize', this.onResize);
    }
  }

  ngOnDestroy() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', this.onResize);
    }
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadOrder(id);
  }

  private loadOrder(id: number) {
    this.isLoading.set(true);
    this.orderService.getOrderDetail(id).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((res) => {
      this.order.set(res);
      this.isLoading.set(false);
      const subId = res?.subOrders[0]?.id;
      if (subId) this.loadReturn(subId);
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
    const note = window.prompt('Dispute note:');
    if (!note) return;
    this.orderService.disputeReturn(r.id, note).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.loadReturn(subId));
  }

  cancelOrder() {
    const o = this.order();
    if (!o) return;
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
    const reason = window.prompt('Return reason:');
    if (!reason) return;
    this.orderService.requestReturn(o.id, subId, reason).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.loadOrder(o.id);
      this.loadReturn(subId);
    });
  }

  openReviewForm(item: OrderItem) {
    const modalRef = this.modal.create({
      nzTitle: 'Đánh giá sản phẩm',
      nzContent: ReviewFormComponent,
      nzViewContainerRef: this.viewContainerRef,
      nzFooter: null,
      nzWidth: 520,
      nzData: {
        orderId: this.order()!.id,
        orderItemId: item.id,
        productName: item.name,
        productImage: item.imageUrl,
      },
    });

    const instance = modalRef.getContentComponent() as ReviewFormComponent;
    instance.close = () => modalRef.destroy();
  }

  protected readonly DateHelper = DateHelper;
}
