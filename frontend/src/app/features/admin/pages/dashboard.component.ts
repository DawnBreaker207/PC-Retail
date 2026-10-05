import { Component, inject, DestroyRef, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { ThemeService } from '@core/services/theme.service';
import { AuthService } from '@core/auth/auth.service';
import { AdminWorkQueueService } from '@core/services/admin-work-queue.service';
import { AdminReturnRequest, Order } from '@core/models/common.model';
import { NgxEchartsDirective, NGX_ECHARTS_CONFIG } from 'ngx-echarts';
import { BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsCoreOption } from 'echarts/core';
import { use, init } from 'echarts/core';

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer]);

const echartsCore = { init };

interface DashboardSummary {
  todayRevenue: number;
  yesterdayRevenue: number;
  todayOrders: number;
  yesterdayOrders: number;
  newCustomers: number;
  lowStockCount: number;
  recentOrders: { id: number; customerName: string; total: number; status: string; createdAt: string }[];
}

interface RevenuePoint { date: string; revenue: number; }
interface LowStockItem { skuId: number; name: string; stock: number; imageUrl: string; price: number; }

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink, DatePipe, NzCardModule, NzStatisticModule, NzGridModule, NzIconModule,
    NzTableModule, NzTagModule, NzSpinModule, NzTabsModule, CurrencyFormatPipe, NgxEchartsDirective,
  ],
  providers: [{
    provide: NGX_ECHARTS_CONFIG,
    useValue: { echarts: echartsCore },
  }],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private themeService = inject(ThemeService);
  private authService = inject(AuthService);
  private workQueue = inject(AdminWorkQueueService);

  summary: DashboardSummary | null = null;
  revenue7 = signal<RevenuePoint[]>([]);
  lowStock: LowStockItem[] = [];
  statusColor: Record<string, string> = {
    PENDING: 'orange', CONFIRMED: 'blue', SHIPPING: 'cyan',
    COMPLETED: 'green', CANCELLED: 'red',
  };

  isAdmin = this.authService.currentUser()?.systemRole === 'ADMIN';
  defaultTabIndex = this.isAdmin ? 0 : 1;

  pendingReturns = signal<AdminReturnRequest[]>([]);
  pendingOrders = signal<Order[]>([]);
  workQueueLoading = signal(false);

  chartOptions = computed<EChartsCoreOption>(() => {
    const data = this.revenue7();
    if (data.length === 0) return {};

    const isDark = this.themeService.isDark();
    const textColor = isDark ? '#8895b0' : '#475569';

    return {
      tooltip: {
        trigger: 'axis',
        valueFormatter: (val: number) => val.toLocaleString('vi-VN') + '₫',
      },
      grid: { left: 4, right: 4, bottom: 20, top: 4, containLabel: true },
      xAxis: {
        type: 'category',
        data: data.map(p => p.date.slice(5)),
        axisLabel: { color: textColor, fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: textColor,
          fontSize: 10,
          formatter: (val: number) =>
            val >= 1_000_000 ? (val / 1_000_000).toFixed(1) + 'tr' : (val / 1_000).toFixed(0) + 'k',
        },
        splitLine: {
          lineStyle: { color: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)' },
        },
        axisLine: { show: false },
        axisTick: { show: false },
      },
      series: [{
        type: 'bar',
        data: data.map(p => p.revenue),
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#e53935' },
        emphasis: { itemStyle: { color: '#ef5350' } },
      }],
    };
  });

  constructor() {
    this.http.get<DashboardSummary>(ApiEndpoints.admin.dashboard).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((d) => {
      this.summary = d;
      this.loadRevenue();
      this.loadLowStock();
    });
    this.loadWorkQueue();
  }

  private loadWorkQueue() {
    this.workQueueLoading.set(true);
    this.workQueue.getPendingReturns().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((items) => {
      this.pendingReturns.set(items);
      this.workQueueLoading.set(false);
    });
    this.workQueue.getPendingPaymentOrders().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((items) => {
      this.pendingOrders.set(items);
    });
  }

  private loadRevenue() {
    this.http.get<RevenuePoint[]>(ApiEndpoints.admin.revenue).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((points) => {
      this.revenue7.set(points);
    });
  }

  private loadLowStock() {
    this.http.get<LowStockItem[]>(ApiEndpoints.inventory.lowStock).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((items) => this.lowStock = items);
  }
}
