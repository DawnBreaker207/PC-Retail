import { Component, inject, DestroyRef, signal } from '@angular/core';
import { Location } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzMessageService } from 'ng-zorro-antd/message';
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
  imports: [
    RouterLink, FormsModule, FallbackImageDirective,
    NzStepsModule, NzTableModule, NzTagModule, NzButtonModule, NzSpinModule,
    NzInputModule, NzCardModule, NzDividerModule,
    CurrencyFormatPipe, StatusFormatPipe,
  ],
  templateUrl: './admin-order-detail.component.html',
})
export class AdminOrderDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  private location = inject(Location);

  order = signal<Order | null>(null);
  loading = signal(false);
  trackingCode = signal('');
  updating = signal(false);

  protected readonly STATUS_STEP_MAP = STATUS_STEP_MAP;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadOrder(id);
  }

  private loadOrder(id: number) {
    this.loading.set(true);
    this.http.get<Order>(ApiEndpoints.staffOrders.detail(id))
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
    const status = this.order()?.subOrders[0]?.status;
    return STATUS_STEP_MAP[status ?? ''] ?? 0;
  }

  updateStatus(status: string) {
    const o = this.order();
    if (!o) return;
    this.updating.set(true);
    const body: Record<string, unknown> = { status };
    if (status === 'SHIPPED' && this.trackingCode()) {
      body['trackingCode'] = this.trackingCode();
    }
    this.http.post(ApiEndpoints.staffOrders.transition(o.id), body)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Cập nhật trạng thái thành công');
          this.loadOrder(o.id);
          this.updating.set(false);
        },
        error: () => {
          this.message.error('Cập nhật thất bại');
          this.updating.set(false);
        },
      });
  }

  get flatItems() {
    return this.order()?.subOrders.flatMap((s) => s.items) ?? [];
  }

  goBack() {
    this.location.back();
  }
}
