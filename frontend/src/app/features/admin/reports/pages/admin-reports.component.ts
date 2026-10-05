import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-admin-reports',
  imports: [FormsModule, NzCardModule, NzTableModule, NzDatePickerModule, NzButtonModule, NzSpinModule, CurrencyFormatPipe],
  template: `
    <nz-card nzTitle="Revenue report">
      <div class="flex gap-2 mb-4">
        <nz-range-picker [(ngModel)]="range" />
        <button nz-button nzType="primary" (click)="load()" [nzLoading]="loading()">Load</button>
        <button nz-button (click)="download('revenue')" [disabled]="range.length < 2">Export revenue CSV</button>
        <button nz-button (click)="download('top-products')" [disabled]="range.length < 2">Export top CSV</button>
      </div>
      <nz-spin [nzSpinning]="loading()">
        <nz-table #t [nzData]="revenue()" [nzFrontPagination]="false">
          <thead>
            <tr>
              <th>Day</th>
              <th>Revenue</th>
            </tr>
          </thead>
          <tbody>
            @for (r of t.data; track r.day) {
              <tr>
                <td>{{ r.day }}</td>
                <td>{{ r.revenue | currencyFormat }}</td>
              </tr>
            }
          </tbody>
        </nz-table>
      </nz-spin>
    </nz-card>
    <nz-card nzTitle="Top products" class="mt-4">
      <nz-table #t2 [nzData]="top()" [nzFrontPagination]="false">
        <thead>
          <tr>
            <th>SKU</th>
            <th>Qty</th>
            <th>Revenue</th>
          </tr>
        </thead>
        <tbody>
          @for (r of t2.data; track r.sku_id) {
            <tr>
              <td>{{ r.sku_id }}</td>
              <td>{{ r.qty }}</td>
              <td>{{ r.revenue | currencyFormat }}</td>
            </tr>
          }
        </tbody>
      </nz-table>
    </nz-card>
  `,
})
export class AdminReportsComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  range: Date[] = [];
  loading = signal(false);
  revenue = signal<{ day: string; revenue: number }[]>([]);
  top = signal<{ sku_id: number; qty: number; revenue: number }[]>([]);

  load() {
    if (this.range.length < 2) return;
    const from = this.range[0].toISOString();
    const to = this.range[1].toISOString();
    this.loading.set(true);
    const params = new HttpParams().set('from', from).set('to', to);
    this.http.get<{ day: string; revenue: number }[]>(ApiEndpoints.reports.revenue, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.revenue.set(Array.isArray(res) ? res : []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    this.http.get<{ sku_id: number; qty: number; revenue: number }[]>(ApiEndpoints.reports.topProducts, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => this.top.set(Array.isArray(res) ? res : []) });
  }

  download(type: 'revenue' | 'top-products') {
    if (this.range.length < 2) return;
    const params = new HttpParams()
      .set('from', this.range[0].toISOString())
      .set('to', this.range[1].toISOString())
      .set('type', type);
    this.http.get(ApiEndpoints.reports.export, { params, responseType: 'text' }).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((csv) => {
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
