import { Component, inject, OnInit, signal, ViewContainerRef } from '@angular/core';
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
import { Order, OrderItem } from '@core/models/common.model';
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
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private modal = inject(NzModalService);
  private viewContainerRef = inject(ViewContainerRef);

  order = signal<Order | null>(null);
  isLoading = signal(false);
  windowWidth = signal(window.innerWidth);

  constructor() {
    if (typeof window !== 'undefined') {
      const onResize = () => this.windowWidth.set(window.innerWidth);
      window.addEventListener('resize', onResize);
    }
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadOrder(id);
  }

  private loadOrder(id: number) {
    this.isLoading.set(true);
    this.orderService.getOrderDetail(id).subscribe((res) => {
      this.order.set(res);
      this.isLoading.set(false);
    });
  }

  cancelOrder() {
    const o = this.order();
    if (!o) return;
    this.orderService.cancelOrder(o.id).subscribe(() => this.loadOrder(o.id));
  }

  get currentStep(): number {
    const status = this.order()?.subOrders[0]?.status;
    return STATUS_STEP_MAP[status ?? ''] ?? 0;
  }

  get isCancellable(): boolean {
    const status = this.order()?.subOrders[0]?.status;
    return status === 'PENDING_PAYMENT' || status === 'PAID';
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
