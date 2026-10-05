import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface CompareSku {
  id: number;
  name: string;
  price: number;
  imageUrls?: string[];
  brandName?: string;
  specs?: Record<string, string>;
}

@Component({
  selector: 'app-compare',
  imports: [RouterLink, NzTableModule, NzEmptyModule, NzBreadCrumbModule, CurrencyFormatPipe, FallbackImageDirective],
  template: `
    <nz-breadcrumb>
      <nz-breadcrumb-item><a routerLink="/">Home</a></nz-breadcrumb-item>
      <nz-breadcrumb-item>Compare</nz-breadcrumb-item>
    </nz-breadcrumb>
    @if (!skus().length) {
      <nz-empty nzNotFoundContent="Select 2-4 products to compare"></nz-empty>
    } @else {
      <nz-table #t [nzData]="rows()" [nzFrontPagination]="false" nzBordered>
        <thead>
          <tr>
            <th>Spec</th>
            @for (s of skus(); track s.id) {
              <th>
                <img [src]="s.imageUrls?.[0]" appFallbackImage class="h-20 object-contain" alt="" />
                <div>{{ s.name }}</div>
                <div>{{ s.price | currencyFormat }}</div>
              </th>
            }
          </tr>
        </thead>
        <tbody>
          @for (r of rows(); track r.key) {
            <tr>
              <td>
                <strong>{{ r.key }}</strong>
              </td>
              @for (v of r.values; track $index) {
                <td>{{ v }}</td>
              }
            </tr>
          }
        </tbody>
      </nz-table>
    }
  `,
})
export class CompareComponent {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  skus = signal<CompareSku[]>([]);
  rows = signal<{ key: string; values: string[] }[]>([]);

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      const ids = params.get('ids')?.split(',').map(Number).filter(Boolean).slice(0, 4) ?? [];
      if (ids.length < 2) {
        this.skus.set([]);
        this.rows.set([]);
        return;
      }
      const httpParams = new HttpParams().set('ids', ids.join(','));
      this.http.get<CompareSku[]>(ApiEndpoints.catalog.compare, { params: httpParams }).pipe(
        catchError(() => of([] as CompareSku[])),
      ).subscribe((list) => {
        const arr = Array.isArray(list) ? list : [];
        this.skus.set(arr);
        const keys = [...new Set(arr.flatMap((s) => Object.keys(s.specs ?? {})))];
        this.rows.set(keys.map((k) => ({ key: k, values: arr.map((s) => s.specs?.[k] ?? '-') })));
      });
    });
  }
}
