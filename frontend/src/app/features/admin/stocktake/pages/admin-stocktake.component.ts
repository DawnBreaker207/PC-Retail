import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzMessageService } from 'ng-zorro-antd/message';
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
  imports: [FormsModule, NzCardModule, NzTableModule, NzButtonModule, NzInputModule, NzSpinModule],
  template: `
    <nz-card nzTitle="Stock take session">
      <div class="flex gap-2 mb-4 flex-wrap">
        <input nz-input [(ngModel)]="skuId" type="number" placeholder="SKU ID" class="max-w-[140px]" />
        <input nz-input [(ngModel)]="counted" type="number" placeholder="Counted qty" class="max-w-[140px]" />
        <button nz-button (click)="addRow()">Add row</button>
        <button nz-button nzType="primary" (click)="start()" [disabled]="!rows().length">Start session</button>
        @if (sessionId()) {
          <button nz-button nzType="primary" nzDanger (click)="complete()">Complete</button>
        }
      </div>
      <nz-table #t [nzData]="rows()" [nzFrontPagination]="false" nzSize="small">
        <thead>
          <tr>
            <th>SKU</th>
            <th>Counted</th>
          </tr>
        </thead>
        <tbody>
          @for (r of t.data; track r.skuId) {
            <tr>
              <td>{{ r.skuId }}</td>
              <td>{{ r.countedQuantity }}</td>
            </tr>
          }
        </tbody>
      </nz-table>
    </nz-card>
    @if (sessionId()) {
      <nz-card nzTitle="Variances (sorted by abs)" class="mt-4">
        <nz-spin [nzSpinning]="loading()">
          <nz-table #v [nzData]="variances()" [nzFrontPagination]="false" nzSize="small">
            <thead>
              <tr>
                <th>SKU</th>
                <th>System</th>
                <th>Counted</th>
                <th>Variance</th>
              </tr>
            </thead>
            <tbody>
              @for (r of v.data; track r.sku_id) {
                <tr>
                  <td>{{ r.sku_id }}</td>
                  <td>{{ r.system_quantity }}</td>
                  <td>{{ r.counted_quantity }}</td>
                  <td>{{ r.variance }}</td>
                </tr>
              }
            </tbody>
          </nz-table>
        </nz-spin>
      </nz-card>
    }
  `,
})
export class AdminStocktakeComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  rows = signal<{ skuId: number; countedQuantity: number }[]>([]);
  variances = signal<VarianceRow[]>([]);
  loading = signal(false);
  sessionId = signal<number | null>(null);
  skuId: number | null = null;
  counted: number | null = null;

  addRow() {
    if (this.skuId == null || this.counted == null || this.counted < 0) {
      this.message.error('Invalid row');
      return;
    }
    this.rows.update((arr) => [...arr, { skuId: this.skuId as number, countedQuantity: this.counted as number }]);
    this.skuId = null;
    this.counted = null;
  }

  start() {
    this.http.post<{ id: number }>(ApiEndpoints.stockTakes.base, { items: this.rows() }).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((s) => {
      if (s) {
        this.sessionId.set(s.id);
        this.loadVariances(s.id);
      } else {
        this.message.error('Start session failed');
      }
    });
  }

  complete() {
    const id = this.sessionId();
    if (!id) return;
    this.http.post(ApiEndpoints.stockTakes.complete(id), {}).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((s) => {
      if (s) {
        this.message.success('Session completed');
        this.rows.set([]);
        this.loadVariances(id);
      } else {
        this.message.error('Complete failed');
      }
    });
  }

  loadVariances(id: number) {
    this.loading.set(true);
    this.http.get<VarianceRow[]>(ApiEndpoints.stockTakes.variances(id)).pipe(
      catchError(() => of([] as VarianceRow[])),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (rows) => {
        this.variances.set(Array.isArray(rows) ? rows : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
