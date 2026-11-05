import {
  Component,
  inject,
  OnInit,
  signal,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule, ShoppingBag, ArrowRight } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
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
    RouterLink,
    FallbackImageDirective,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
    StatusFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './order-list.component.html',
})
export class OrderListComponent implements OnInit {
  readonly ShoppingBag = ShoppingBag;
  readonly ArrowRight = ArrowRight;

  private orderService = inject(OrderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  orders = signal<Order[]>([]);
  isLoading = signal(false);
  activeTab = signal('');
  statusTabs = STATUS_TABS;

  ngOnInit() {
    this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.activeTab.set(params['status'] || '');
      this.loadOrders();
    });
  }

  private loadOrders() {
    this.isLoading.set(true);
    this.orderService
      .getMyOrders({ status: this.activeTab() || undefined })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.orders.set(res.content);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
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
