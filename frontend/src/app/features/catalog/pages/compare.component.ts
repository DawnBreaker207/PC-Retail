import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { LucideAngularModule, ChevronRight, Scale, ArrowLeft } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
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
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective, CurrencyFormatPipe, FallbackImageDirective],
  template: `
    <div class="max-w-screen-2xl mx-auto px-4 py-6">
      <nav class="flex items-center gap-1.5 text-xs text-muted dark:text-muted-dark mb-4">
        <a routerLink="/" class="hover:text-primary transition-colors">Trang chủ</a>
        <lucide-angular [img]="ChevronRight" class="w-3.5 h-3.5 text-muted/60" />
        <span class="text-ink dark:text-ink-dark font-medium">So sánh sản phẩm</span>
      </nav>

      <h1 class="text-2xl font-bold text-ink dark:text-ink-dark mb-6">So sánh sản phẩm</h1>

      @if (!skus().length) {
        <div class="text-center py-16 px-4 bg-surface dark:bg-surface-dark rounded-card border border-border/60 dark:border-border-dark/60">
          <div class="w-14 h-14 rounded-full bg-muted/10 text-muted dark:text-muted-dark flex items-center justify-center mx-auto mb-4">
            <lucide-angular [img]="Scale" class="w-7 h-7" />
          </div>
          <h3 class="text-base font-semibold text-ink dark:text-ink-dark mb-1">Chưa chọn sản phẩm để so sánh</h3>
          <p class="text-xs text-muted dark:text-muted-dark mb-5">Vui lòng chọn từ 2 đến 4 sản phẩm để bắt đầu so sánh chi tiết thông số.</p>
          <a hlmBtn variant="outline" routerLink="/products">
            <lucide-angular [img]="ArrowLeft" class="w-4 h-4 mr-2" />
            Khám phá sản phẩm
          </a>
        </div>
      } @else {
        <div class="overflow-x-auto rounded-card border border-border dark:border-border-dark bg-surface dark:bg-surface-dark shadow-sm">
          <table class="w-full text-sm text-left border-collapse">
            <thead>
              <tr class="border-b border-border dark:border-border-dark bg-bg/50 dark:bg-bg-dark/50">
                <th class="p-4 w-48 min-w-[160px] font-semibold text-muted dark:text-muted-dark text-xs uppercase tracking-wider">Thông số kỹ thuật</th>
                @for (s of skus(); track s.id) {
                  <th class="p-4 min-w-[220px] max-w-[300px] align-top">
                    <div class="aspect-square w-24 mx-auto mb-3 flex items-center justify-center bg-surface dark:bg-surface-dark rounded-md border border-border/50 overflow-hidden">
                      <img [src]="s.imageUrls?.[0]" appFallbackImage class="max-h-full max-w-full object-contain p-2" alt="{{ s.name }}" />
                    </div>
                    <div class="font-medium text-ink dark:text-ink-dark text-xs line-clamp-2 hover:text-primary transition-colors mb-1">{{ s.name }}</div>
                    <div class="text-primary font-bold text-sm">{{ s.price | currencyFormat }}</div>
                  </th>
                }
              </tr>
            </thead>
            <tbody class="divide-y divide-border/60 dark:divide-border-dark/60">
              @for (r of rows(); track r.key) {
                <tr class="hover:bg-bg/40 dark:hover:bg-bg-dark/40 transition-colors">
                  <td class="p-4 font-medium text-ink dark:text-ink-dark bg-bg/20 dark:bg-bg-dark/20 text-xs">
                    {{ r.key }}
                  </td>
                  @for (v of r.values; track $index) {
                    <td class="p-4 text-xs text-muted dark:text-muted-dark">{{ v }}</td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
})
export class CompareComponent {
  readonly ChevronRight = ChevronRight;
  readonly Scale = Scale;
  readonly ArrowLeft = ArrowLeft;

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

