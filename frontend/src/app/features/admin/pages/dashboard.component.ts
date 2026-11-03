import { Component, inject, DestroyRef, signal, computed } from '@angular/core';
import { DatePipe, CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { StatusFormatPipe } from '@shared/pipes/status-format.pipe';
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
import {
  HlmCardDirective,
  HlmCardHeaderDirective,
  HlmCardTitleDirective,
  HlmCardContentDirective
} from '@shared/ui/card';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective
} from '@shared/ui/table';
import {
  LucideAngularModule,
  TrendingUp,
  ShoppingCart,
  Users,
  AlertTriangle,
  Loader2,
  Calendar,
  Layers,
  ArrowRight
} from 'lucide-angular';

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
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DatePipe,
    CurrencyFormatPipe,
    StatusFormatPipe,
    NgxEchartsDirective,
    HlmCardDirective,
    HlmBadgeDirective,
    HlmButtonDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    LucideAngularModule
  ],
  providers: [{
    provide: NGX_ECHARTS_CONFIG,
    useValue: { echarts: echartsCore },
  }],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  readonly TrendingUp = TrendingUp;
  readonly ShoppingCart = ShoppingCart;
  readonly Users = Users;
  readonly AlertTriangle = AlertTriangle;
  readonly Loader2 = Loader2;
  readonly Calendar = Calendar;
  readonly Layers = Layers;
  readonly ArrowRight = ArrowRight;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private themeService = inject(ThemeService);
  private authService = inject(AuthService);
  private workQueue = inject(AdminWorkQueueService);

  summary: DashboardSummary | null = null;
  revenue7 = signal<RevenuePoint[]>([]);
  lowStock: LowStockItem[] = [];

  isAdmin = this.authService.currentUser()?.systemRole === 'ADMIN';
  activeTab = signal<'analytics' | 'queue'>('analytics');

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
