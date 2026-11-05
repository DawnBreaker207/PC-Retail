import { Component, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  Download,
  Calendar,
  TrendingUp,
  Award,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    CurrencyFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="mb-6">
      <h1 class="text-2xl font-bold tracking-tight text-foreground">Báo cáo & Phân tích</h1>
      <p class="text-muted-foreground text-sm mt-1">
        Theo dõi doanh thu bán hàng và sản phẩm bán chạy theo mốc thời gian
      </p>
    </div>

    <!-- Date Range & Actions Filter -->
    <div class="bg-card rounded-xl border border-border/80 shadow-2xs p-5 mb-6">
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex items-center gap-2">
          <label class="text-xs font-medium text-foreground">Từ ngày:</label>
          <input hlmInput type="date" [(ngModel)]="fromDate" class="h-9 px-3 text-xs w-36" />
        </div>
        <div class="flex items-center gap-2">
          <label class="text-xs font-medium text-foreground">Đến ngày:</label>
          <input hlmInput type="date" [(ngModel)]="toDate" class="h-9 px-3 text-xs w-36" />
        </div>

        <button
          hlmBtn
          variant="default"
          size="sm"
          (click)="load()"
          [disabled]="loading() || !fromDate || !toDate"
          class="h-9 gap-1.5 text-xs bg-primary text-primary-foreground cursor-pointer"
        >
          <lucide-icon [img]="Calendar" class="w-3.5 h-3.5"></lucide-icon>
          Tải dữ liệu
        </button>

        <button
          hlmBtn
          variant="outline"
          size="sm"
          (click)="download('revenue')"
          [disabled]="!fromDate || !toDate"
          class="h-9 gap-1.5 text-xs cursor-pointer"
        >
          <lucide-icon [img]="Download" class="w-3.5 h-3.5"></lucide-icon>
          Xuất CSV Doanh thu
        </button>

        <button
          hlmBtn
          variant="outline"
          size="sm"
          (click)="download('top-products')"
          [disabled]="!fromDate || !toDate"
          class="h-9 gap-1.5 text-xs cursor-pointer"
        >
          <lucide-icon [img]="Download" class="w-3.5 h-3.5"></lucide-icon>
          Xuất CSV Top sản phẩm
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Bảng Doanh thu theo ngày -->
      <div class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden">
        <div class="p-4 border-b border-border bg-muted/20 flex items-center gap-2">
          <lucide-icon [img]="TrendingUp" class="w-4 h-4 text-primary"></lucide-icon>
          <h2 class="text-sm font-semibold text-foreground">Doanh thu theo ngày</h2>
        </div>
        @if (loading()) {
          <div class="flex flex-col items-center justify-center py-16 gap-2 text-muted-foreground">
            <lucide-icon [img]="Loader2" class="w-6 h-6 animate-spin text-primary"></lucide-icon>
            <span class="text-xs">Đang nạp báo cáo...</span>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table hlmTable class="w-full text-xs">
              <thead hlmTableHeader>
                <tr hlmTableRow class="border-b border-border bg-muted/40">
                  <th hlmTableHead>Ngày ghi nhận</th>
                  <th hlmTableHead class="text-right">Tổng doanh thu</th>
                </tr>
              </thead>
              <tbody hlmTableBody>
                @for (r of revenue(); track r.day) {
                  <tr hlmTableRow class="border-b border-border/60 hover:bg-muted/20">
                    <td hlmTableCell class="font-medium text-foreground">{{ r.day }}</td>
                    <td hlmTableCell class="text-right font-bold text-primary">
                      {{ r.revenue | currencyFormat }}
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="2" class="text-center py-10 text-muted-foreground italic">
                      Chưa có dữ liệu báo cáo trong khoảng thời gian này
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <!-- Bảng Top sản phẩm bán chạy -->
      <div class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden">
        <div class="p-4 border-b border-border bg-muted/20 flex items-center gap-2">
          <lucide-icon [img]="Award" class="w-4 h-4 text-amber-500"></lucide-icon>
          <h2 class="text-sm font-semibold text-foreground">Top sản phẩm bán chạy</h2>
        </div>
        <div class="overflow-x-auto">
          <table hlmTable class="w-full text-xs">
            <thead hlmTableHeader>
              <tr hlmTableRow class="border-b border-border bg-muted/40">
                <th hlmTableHead>Mã SKU</th>
                <th hlmTableHead class="text-center">Số lượng bán</th>
                <th hlmTableHead class="text-right">Doanh thu tích lũy</th>
              </tr>
            </thead>
            <tbody hlmTableBody>
              @for (r of top(); track r.sku_id) {
                <tr hlmTableRow class="border-b border-border/60 hover:bg-muted/20">
                  <td hlmTableCell class="font-mono font-semibold text-foreground">
                    #SKU-{{ r.sku_id }}
                  </td>
                  <td hlmTableCell class="text-center">
                    <span
                      class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/10 text-primary"
                    >
                      {{ r.qty }}
                    </span>
                  </td>
                  <td hlmTableCell class="text-right font-bold text-primary">
                    {{ r.revenue | currencyFormat }}
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="3" class="text-center py-10 text-muted-foreground italic">
                    Chưa có sản phẩm nào ghi nhận doanh thu
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
})
export class AdminReportsComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  readonly Download = Download;
  readonly Calendar = Calendar;
  readonly TrendingUp = TrendingUp;
  readonly Award = Award;
  readonly Loader2 = Loader2;

  fromDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  toDate = new Date().toISOString().split('T')[0];

  loading = signal(false);
  revenue = signal<{ day: string; revenue: number }[]>([]);
  top = signal<{ sku_id: number; qty: number; revenue: number }[]>([]);

  constructor() {
    this.load();
  }

  load() {
    if (!this.fromDate || !this.toDate) return;
    this.loading.set(true);
    const params = new HttpParams()
      .set('from', new Date(this.fromDate).toISOString())
      .set('to', new Date(this.toDate).toISOString());

    this.http
      .get<{ day: string; revenue: number }[]>(ApiEndpoints.reports.revenue, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.revenue.set(Array.isArray(res) ? res : []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });

    this.http
      .get<{ sku_id: number; qty: number; revenue: number }[]>(ApiEndpoints.reports.topProducts, {
        params,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => this.top.set(Array.isArray(res) ? res : []),
      });
  }

  download(type: 'revenue' | 'top-products') {
    if (!this.fromDate || !this.toDate) return;
    const params = new HttpParams()
      .set('from', new Date(this.fromDate).toISOString())
      .set('to', new Date(this.toDate).toISOString())
      .set('type', type);

    this.http
      .get(ApiEndpoints.reports.export, { params, responseType: 'text' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((csv) => {
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${type}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }
}
