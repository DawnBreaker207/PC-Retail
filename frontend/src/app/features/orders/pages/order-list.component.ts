import { Component, inject, OnInit, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { StatusFormatPipe } from '@shared/pipes/status-format.pipe';
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
    CurrencyFormatPipe, StatusFormatPipe,
  ],
  templateUrl: './order-list.component.html',
})
export class OrderListComponent implements OnInit {
  private orderService = inject(OrderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  orders = signal<Order[]>([]);
  isLoading = signal(false);
  activeTab = signal('');
  statusTabs = STATUS_TABS;

  activeTabIndex = computed(() => {
    const idx = this.statusTabs.findIndex((t) => t.key === this.activeTab());
    return Math.max(0, idx);
  });

  ngOnInit() {
    this.route.queryParams.pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((params) => {
      this.activeTab.set(params['status'] || '');
      this.loadOrders();
    });
  }

  private loadOrders() {
    this.isLoading.set(true);
    this.orderService
      .getMyOrders({ status: this.activeTab() || undefined }).pipe(
        takeUntilDestroyed(this.destroyRef),
      )
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

  protected readonly DateHelper = DateHelper;
}
