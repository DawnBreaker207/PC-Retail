import { Component, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  ClipboardCheck,
  Plus,
  Play,
  CheckCircle,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import { ToastService } from '@core/services/toast.service';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface VarianceRow {
  sku_id: number;
  system_quantity: number;
  counted_quantity: number;
  variance: number;
}

@Component({
  selector: 'app-admin-stocktake',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="mb-6">
      <h1 class="text-2xl font-bold tracking-tight text-foreground">
        Kiểm kê kho & Đối soát chênh lệch
      </h1>
      <p class="text-muted-foreground text-sm mt-1">
        Ghi nhận số lượng thực tế tại kho và phân tích độ lệch so với dữ liệu hệ thống
      </p>
    </div>

    <!-- Phiên kiểm kê kho -->
    <div class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden mb-6">
      <div class="p-4 border-b border-border bg-muted/20 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <lucide-icon [img]="ClipboardCheck" class="w-4 h-4 text-primary"></lucide-icon>
          <h2 class="text-sm font-semibold text-foreground">Phiên kiểm kê kho</h2>
        </div>
        @if (sessionId()) {
          <span
            hlmBadge
            variant="outline"
            class="border-primary bg-primary/10 text-primary text-xs px-2.5 py-0.5 rounded-full font-mono font-bold"
          >
            Phiên đang mở #{{ sessionId() }}
          </span>
        }
      </div>

      <div class="p-4">
        <div class="flex flex-wrap items-center gap-3 mb-5">
          <input
            hlmInput
            [(ngModel)]="skuId"
            type="number"
            placeholder="Mã SKU (VD: 101)..."
            class="w-44 h-9 text-xs"
          />
          <input
            hlmInput
            [(ngModel)]="counted"
            type="number"
            placeholder="Số lượng thực đếm..."
            class="w-48 h-9 text-xs"
          />
          <button
            hlmBtn
            variant="outline"
            size="sm"
            (click)="addRow()"
            class="h-9 px-3 gap-1.5 text-xs cursor-pointer"
          >
            <lucide-icon [img]="Plus" class="w-3.5 h-3.5"></lucide-icon>
            Thêm dòng
          </button>
          <button
            hlmBtn
            variant="default"
            size="sm"
            (click)="start()"
            [disabled]="!rows().length"
            class="h-9 px-3.5 gap-1.5 text-xs bg-primary text-white text-primary-foreground cursor-pointer"
          >
            <lucide-icon [img]="Play" class="w-3.5 h-3.5"></lucide-icon>
            Bắt đầu kiểm kê
          </button>

          @if (sessionId()) {
            <button
              hlmBtn
              variant="destructive"
              size="sm"
              (click)="complete()"
              class="h-9 px-3.5 gap-1.5 text-xs bg-destructive text-white text-destructive-foreground cursor-pointer"
            >
              <lucide-icon [img]="CheckCircle" class="w-3.5 h-3.5"></lucide-icon>
              Hoàn tất kiểm kê (#{{ sessionId() }})
            </button>
          }
        </div>

        <div class="overflow-x-auto rounded-lg border border-border/60">
          <table hlmTable class="w-full text-xs">
            <thead hlmTableHeader>
              <tr hlmTableRow class="border-b border-border bg-muted/30">
                <th hlmTableHead class="w-44">Mã SKU</th>
                <th hlmTableHead class="text-right">Số lượng thực đếm</th>
              </tr>
            </thead>
            <tbody hlmTableBody>
              @for (r of rows(); track r.skuId) {
                <tr hlmTableRow class="border-b border-border/50 hover:bg-muted/20">
                  <td hlmTableCell class="font-mono font-medium text-foreground">
                    #SKU-{{ r.skuId }}
                  </td>
                  <td hlmTableCell class="text-right font-bold text-primary">
                    {{ r.countedQuantity }}
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="2" class="text-center text-muted-foreground py-6 italic text-xs">
                    Chưa có dòng nào. Nhập mã SKU và số lượng bên trên để bắt đầu phiên kiểm kê.
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>

    @if (sessionId()) {
      <!-- Bảng đối soát chênh lệch -->
      <div class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden">
        <div class="p-4 border-b border-border bg-muted/20">
          <h2 class="text-sm font-semibold text-foreground">
            Bảng đối soát chênh lệch tồn kho (Hệ thống vs Thực đếm)
          </h2>
        </div>

        @if (loading()) {
          <div class="flex flex-col items-center justify-center py-16 gap-2 text-muted-foreground">
            <lucide-icon [img]="Loader2" class="w-6 h-6 animate-spin text-primary"></lucide-icon>
            <span class="text-xs">Đang tính toán chênh lệch...</span>
          </div>
        } @else {
          <div class="overflow-x-auto p-4">
            <div class="rounded-lg border border-border/60 overflow-hidden">
              <table hlmTable class="w-full text-xs">
                <thead hlmTableHeader>
                  <tr hlmTableRow class="border-b border-border bg-muted/30">
                    <th hlmTableHead class="w-32">Mã SKU</th>
                    <th hlmTableHead class="text-center w-36">Tồn hệ thống</th>
                    <th hlmTableHead class="text-center w-36">Tồn thực đếm</th>
                    <th hlmTableHead class="text-center w-40">Chênh lệch (Variance)</th>
                  </tr>
                </thead>
                <tbody hlmTableBody>
                  @for (r of variances(); track r.sku_id) {
                    <tr hlmTableRow class="border-b border-border/50 hover:bg-muted/20">
                      <td hlmTableCell class="font-mono font-semibold text-foreground">
                        #SKU-{{ r.sku_id }}
                      </td>
                      <td hlmTableCell class="text-center font-medium text-foreground">
                        {{ r.system_quantity }}
                      </td>
                      <td hlmTableCell class="text-center font-bold text-foreground">
                        {{ r.counted_quantity }}
                      </td>
                      <td hlmTableCell class="text-center">
                        @if (r.variance === 0) {
                          <span
                            hlmBadge
                            variant="outline"
                            class="border-emerald-400 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 text-[11px] px-2.5 py-0.5 rounded-full font-medium"
                          >
                            Khớp (0)
                          </span>
                        } @else if (r.variance > 0) {
                          <span
                            hlmBadge
                            variant="outline"
                            class="border-amber-400 bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400 text-[11px] px-2.5 py-0.5 rounded-full font-medium"
                          >
                            Thừa (+{{ r.variance }})
                          </span>
                        } @else {
                          <span
                            hlmBadge
                            variant="outline"
                            class="border-rose-400 bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 text-[11px] px-2.5 py-0.5 rounded-full font-medium"
                          >
                            Thiếu ({{ r.variance }})
                          </span>
                        }
                      </td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="4" class="text-center py-8 text-muted-foreground italic">
                        Không có số liệu chênh lệch nào được ghi nhận
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }
      </div>
    }
  `,
})
export class AdminStocktakeComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly ClipboardCheck = ClipboardCheck;
  readonly Plus = Plus;
  readonly Play = Play;
  readonly CheckCircle = CheckCircle;
  readonly Loader2 = Loader2;

  rows = signal<{ skuId: number; countedQuantity: number }[]>([]);
  variances = signal<VarianceRow[]>([]);
  loading = signal(false);
  sessionId = signal<number | null>(null);
  skuId: number | null = null;
  counted: number | null = null;

  addRow() {
    if (this.skuId == null || this.counted == null || this.counted < 0) {
      this.toast.error('Vui lòng nhập mã SKU và số lượng hợp lệ (>= 0)');
      return;
    }
    this.rows.update((arr) => [
      ...arr,
      { skuId: this.skuId as number, countedQuantity: this.counted as number },
    ]);
    this.skuId = null;
    this.counted = null;
  }

  start() {
    this.http
      .post<{ id: number }>(ApiEndpoints.stockTakes.base, { items: this.rows() })
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((s) => {
        if (s) {
          this.sessionId.set(s.id);
          this.toast.success(`Đã khởi tạo phiên kiểm kê #${s.id}`);
          this.loadVariances(s.id);
        } else {
          this.toast.error('Khởi tạo phiên kiểm kê thất bại');
        }
      });
  }

  complete() {
    const id = this.sessionId();
    if (!id) return;
    this.http
      .post(ApiEndpoints.stockTakes.complete(id), {})
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((s) => {
        if (s) {
          this.toast.success('Đã hoàn tất phiên kiểm kê và chốt số liệu');
          this.rows.set([]);
          this.loadVariances(id);
        } else {
          this.toast.error('Hoàn tất phiên kiểm kê thất bại');
        }
      });
  }

  loadVariances(id: number) {
    this.loading.set(true);
    this.http
      .get<VarianceRow[]>(ApiEndpoints.stockTakes.variances(id))
      .pipe(
        catchError(() => of([] as VarianceRow[])),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (rows) => {
          this.variances.set(Array.isArray(rows) ? rows : []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
