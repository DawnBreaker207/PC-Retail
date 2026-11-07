import { Component, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  Building2,
  ShoppingBag,
  Plus,
  Check,
  PackageCheck,
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
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface Supplier {
  id: number;
  name: string;
}

interface PurchaseOrder {
  id: number;
  supplierId: number;
  status: string;
  totalCost: number;
}

@Component({
  selector: 'app-admin-purchasing',
  standalone: true,
  imports: [
    NgClass,
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
    CurrencyFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="mb-6">
      <h1 class="text-2xl font-bold tracking-tight text-foreground">
        Mua hàng & Quản lý Nhà cung cấp
      </h1>
      <p class="text-muted-foreground text-sm mt-1">
        Quản lý danh sách nhà cung ứng và theo dõi đơn mua hàng nhập kho
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Cột trái: Quản lý nhà cung cấp -->
      <div class="lg:col-span-1">
        <div
          class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden h-full flex flex-col"
        >
          <div class="p-4 border-b border-border bg-muted/20 flex items-center gap-2">
            <lucide-icon [img]="Building2" class="w-4 h-4 text-primary"></lucide-icon>
            <h2 class="text-sm font-semibold text-foreground">Nhà cung cấp</h2>
          </div>

          <div class="p-4 flex-1">
            <div class="flex gap-2 mb-4">
              <input
                hlmInput
                [(ngModel)]="supplierName"
                placeholder="Tên nhà cung cấp..."
                class="flex-1 h-9 text-xs"
              />
              <button
                hlmBtn
                variant="default"
                size="sm"
                (click)="addSupplier()"
                [disabled]="!supplierName.trim()"
                class="h-9 px-3 gap-1 text-xs bg-primary text-white text-primary-foreground cursor-pointer"
              >
                <lucide-icon [img]="Plus" class="w-3.5 h-3.5"></lucide-icon>
                Thêm
              </button>
            </div>

            <div class="overflow-x-auto rounded-lg border border-border/60">
              <table hlmTable class="w-full text-xs">
                <thead hlmTableHeader>
                  <tr hlmTableRow class="border-b border-border bg-muted/30">
                    <th hlmTableHead class="w-16">Mã</th>
                    <th hlmTableHead>Tên NCC</th>
                  </tr>
                </thead>
                <tbody hlmTableBody>
                  @for (s of suppliers(); track s.id) {
                    <tr hlmTableRow class="border-b border-border/50 hover:bg-muted/20">
                      <td hlmTableCell class="font-mono text-muted-foreground">#{{ s.id }}</td>
                      <td hlmTableCell class="font-medium text-foreground">{{ s.name }}</td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="2" class="text-center py-6 text-muted-foreground italic text-xs">
                        Chưa có nhà cung cấp nào
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Cột phải: Đơn mua hàng -->
      <div class="lg:col-span-2">
        <div
          class="bg-card rounded-xl border border-border/80 shadow-2xs overflow-hidden h-full flex flex-col"
        >
          <div class="p-4 border-b border-border bg-muted/20 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <lucide-icon [img]="ShoppingBag" class="w-4 h-4 text-primary"></lucide-icon>
              <h2 class="text-sm font-semibold text-foreground">
                Đơn đặt mua hàng (Purchase Orders)
              </h2>
            </div>
          </div>

          <div class="p-4 flex-1">
            <div class="flex flex-wrap items-center gap-2.5 mb-4">
              <input
                hlmInput
                [(ngModel)]="newSupplierId"
                type="number"
                placeholder="Nhập ID Nhà cung cấp..."
                class="w-52 h-9 text-xs"
              />
              <button
                hlmBtn
                variant="default"
                size="sm"
                (click)="addOrder()"
                [disabled]="!newSupplierId"
                class="h-9 px-3.5 gap-1.5 text-xs bg-primary text-white text-primary-foreground cursor-pointer"
              >
                <lucide-icon [img]="Plus" class="w-3.5 h-3.5"></lucide-icon>
                Tạo đơn nháp (DRAFT)
              </button>
            </div>

            @if (loading()) {
              <div
                class="flex flex-col items-center justify-center py-16 gap-2 text-muted-foreground"
              >
                <lucide-icon
                  [img]="Loader2"
                  class="w-6 h-6 animate-spin text-primary"
                ></lucide-icon>
                <span class="text-xs">Đang tải danh sách đơn mua...</span>
              </div>
            } @else {
              <div class="overflow-x-auto rounded-lg border border-border/60">
                <table hlmTable class="w-full text-xs">
                  <thead hlmTableHeader>
                    <tr hlmTableRow class="border-b border-border bg-muted/30">
                      <th hlmTableHead class="w-24">Mã đơn</th>
                      <th hlmTableHead>Nhà cung cấp</th>
                      <th hlmTableHead class="w-32 text-center">Trạng thái</th>
                      <th hlmTableHead class="w-32 text-right">Tổng giá trị</th>
                      <th hlmTableHead class="w-32 text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody hlmTableBody>
                    @for (o of orders(); track o.id) {
                      <tr hlmTableRow class="border-b border-border/50 hover:bg-muted/20">
                        <td hlmTableCell class="font-mono font-semibold text-primary">
                          #PO-{{ o.id }}
                        </td>
                        <td hlmTableCell class="font-medium text-foreground">
                          NCC #{{ o.supplierId }}
                        </td>
                        <td hlmTableCell class="text-center">
                          <span
                            hlmBadge
                            variant="outline"
                            class="px-2.5 py-0.5 rounded-full text-[11px] font-medium"
                            [ngClass]="{
                              'border-slate-300 bg-slate-50 text-slate-700 dark:bg-slate-900/40 dark:text-slate-400':
                                o.status === 'DRAFT',
                              'border-blue-400 bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400':
                                o.status === 'CONFIRMED',
                              'border-emerald-400 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400':
                                o.status === 'RECEIVED',
                              'border-rose-400 bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400':
                                o.status === 'CANCELLED',
                            }"
                          >
                            {{ getStatusLabel(o.status) }}
                          </span>
                        </td>
                        <td hlmTableCell class="text-right font-bold text-foreground">
                          {{ o.totalCost | currencyFormat }}
                        </td>
                        <td hlmTableCell class="text-right">
                          <div class="flex items-center justify-end gap-1.5">
                            @if (o.status === 'DRAFT') {
                              <button
                                hlmBtn
                                variant="outline"
                                size="sm"
                                (click)="confirm(o.id)"
                                class="h-7 px-2 text-xs gap-1 cursor-pointer"
                              >
                                <lucide-icon [img]="Check" class="w-3 h-3"></lucide-icon>
                                Duyệt
                              </button>
                            }
                            @if (o.status === 'CONFIRMED') {
                              <button
                                hlmBtn
                                variant="default"
                                size="sm"
                                (click)="receive(o.id)"
                                class="h-7 px-2 text-xs gap-1 bg-emerald-600 hover:bg-emerald-700 text-white cursor-pointer"
                              >
                                <lucide-icon [img]="PackageCheck" class="w-3 h-3"></lucide-icon>
                                Nhập kho
                              </button>
                            }
                          </div>
                        </td>
                      </tr>
                    } @empty {
                      <tr>
                        <td colspan="5" class="text-center py-10 text-muted-foreground italic">
                          Chưa có đơn đặt mua hàng nào
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
})
export class AdminPurchasingComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly Building2 = Building2;
  readonly ShoppingBag = ShoppingBag;
  readonly Plus = Plus;
  readonly Check = Check;
  readonly PackageCheck = PackageCheck;
  readonly Loader2 = Loader2;

  suppliers = signal<Supplier[]>([]);
  orders = signal<PurchaseOrder[]>([]);
  loading = signal(false);
  supplierName = '';
  newSupplierId: number | null = null;

  getStatusLabel(status: string): string {
    switch (status) {
      case 'DRAFT':
        return 'Bản nháp';
      case 'CONFIRMED':
        return 'Đã duyệt';
      case 'RECEIVED':
        return 'Đã nhập kho';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status;
    }
  }

  constructor() {
    this.loadAll();
  }

  loadAll() {
    this.loading.set(true);
    this.http
      .get<Supplier[]>(ApiEndpoints.purchasing.suppliers)
      .pipe(
        catchError(() => of([] as Supplier[])),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((list) => this.suppliers.set(Array.isArray(list) ? list : []));

    this.http
      .get<PurchaseOrder[]>(ApiEndpoints.purchasing.orders)
      .pipe(
        catchError(() => of([] as PurchaseOrder[])),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (list) => {
          this.orders.set(Array.isArray(list) ? list : []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  addSupplier() {
    const name = this.supplierName.trim();
    if (!name) return;
    this.http
      .post<Supplier>(ApiEndpoints.purchasing.suppliers, { name })
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((s) => {
        if (s) {
          this.suppliers.update((arr) => [...arr, s]);
          this.supplierName = '';
          this.toast.success('Đã thêm nhà cung cấp thành công');
        } else {
          this.toast.error('Thêm nhà cung cấp thất bại');
        }
      });
  }

  addOrder() {
    if (!this.newSupplierId) return;
    this.http
      .post<PurchaseOrder>(ApiEndpoints.purchasing.orders, { supplierId: this.newSupplierId })
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((o) => {
        if (o) {
          this.orders.update((arr) => [...arr, o]);
          this.newSupplierId = null;
          this.toast.success('Đã tạo đơn mua hàng thành công');
        } else {
          this.toast.error('Tạo đơn mua hàng thất bại');
        }
      });
  }

  confirm(id: number) {
    this.http
      .post<PurchaseOrder>(ApiEndpoints.purchasing.confirmOrder(id), {})
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((o) => {
        if (o) {
          this.toast.success('Đã duyệt đơn mua hàng');
          this.loadAll();
        } else {
          this.toast.error('Duyệt đơn thất bại');
        }
      });
  }

  receive(id: number) {
    this.http
      .post<PurchaseOrder>(ApiEndpoints.purchasing.receiveOrder(id), {})
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((o) => {
        if (o) {
          this.toast.success('Đã nhập kho thành công');
          this.loadAll();
        } else {
          this.toast.error('Nhập kho thất bại');
        }
      });
  }
}
