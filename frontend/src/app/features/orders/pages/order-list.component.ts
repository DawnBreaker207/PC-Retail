import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { DateHelper } from '@shared/utils/date.helper';
import { OrderService } from '@core/services/order.service';
import { Order } from '@core/models/common.model';

const STATUS_TABS = [
  { key: '', label: 'Tất cả' },
  { key: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { key: 'PROCESSING', label: 'Đang xử lý' },
  { key: 'SHIPPED', label: 'Đang giao' },
  { key: 'DELIVERED', label: 'Đã nhận' },
  { key: 'CANCELLED', label: 'Đã hủy' },
] as const;

const STATUS_COLOR: Record<string, string> = {
  PENDING_PAYMENT: 'gold',
  PAID: 'blue',
  PROCESSING: 'processing',
  SHIPPED: 'purple',
  DELIVERED: 'success',
  CANCELLED: 'error',
  EXPIRED: 'default',
  RETURN_REQUESTED: 'warning',
  RETURNED: 'magenta',
};

@Component({
  selector: 'app-order-list',
  imports: [
    RouterLink, FallbackImageDirective,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzSpinModule,
    NzEmptyModule,
    NzTabsModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './order-list.component.html',
})
export class OrderListComponent implements OnInit {
  private orderService = inject(OrderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  orders = signal<Order[]>([]);
  isLoading = signal(false);
  activeTab = signal('');
  statusTabs = STATUS_TABS;
  statusColor = STATUS_COLOR;

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.activeTab.set(params['status'] || '');
      this.loadOrders();
    });
  }

  private loadOrders() {
    this.isLoading.set(true);
    this.orderService
      .getMyOrders({ status: this.activeTab() || undefined })
      .subscribe((res) => {
        this.orders.set(res.content);
        this.isLoading.set(false);
      });
  }

  onTabChange(key: string) {
    this.router.navigate([], {
      queryParams: { status: key || undefined },
      queryParamsHandling: 'merge',
    });
  }

  getStatusColor(subOrders: { status: string }[]): string {
    return this.statusColor[subOrders[0]?.status] || 'default';
  }

  getStatusLabel(subOrders: { status: string }[]): string {
    return subOrders[0]?.status || '';
  }

  getActiveTabIndex(): number {
    const idx = this.statusTabs.findIndex((t) => t.key === this.activeTab());
    return Math.max(0, idx);
  }

  protected readonly DateHelper = DateHelper;
}
