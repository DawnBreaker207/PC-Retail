import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { MockDataService } from '@core/services/mock-data.service';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';

@Component({
  selector: 'app-flash-sale-section',
  imports: [RouterLink, NzProgressModule, CurrencyFormatPipe, FallbackImageDirective],
  template: `
    <div class="max-w-screen-2xl mx-auto px-4 mt-8 lg:mt-10">
      <div class="bg-surface dark:bg-surface-dark rounded-xl border border-border/80 dark:border-border-dark/80 shadow-sm overflow-hidden">
        <div class="bg-gradient-to-r from-primary to-primary-dark px-5 py-3 flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <div class="bg-white/20 rounded-lg px-2.5 py-1">
              <span class="text-white font-bold text-lg tabular-nums">⚡</span>
            </div>
            <h2 class="text-white font-bold text-lg lg:text-xl tracking-tight">FLASH SALE</h2>
            <div class="hidden sm:flex items-center gap-1.5 bg-white/20 rounded-lg px-2.5 py-1">
              <span class="text-white/80 text-xs font-medium">Kết thúc trong</span>
              <span class="text-white font-bold text-sm tabular-nums">{{ countdown }}</span>
            </div>
          </div>
          <a routerLink="/products" class="text-white/80 hover:text-white text-sm font-medium transition-colors">Xem tất cả →</a>
        </div>

        <div class="p-4">
          <div class="flex flex-wrap gap-1.5 mb-4" role="tablist" aria-label="Flash sale categories">
            @for (tab of flashCategories; track tab; let i = $index) {
              <button
                class="min-h-11 px-4 text-xs font-medium rounded-full border transition-all duration-200 focus-visible:outline-2 focus-visible:outline-primary/60 focus-visible:outline-offset-1"
                [class.bg-primary]="activeTab === i"
                [class.text-white]="activeTab === i"
                [class.border-primary]="activeTab === i"
                [class.bg-surface-card]="activeTab !== i"
                [class.text-muted]="activeTab !== i"
                [class.border-border/80]="activeTab !== i"
                [class.dark:bg-surface-card-dark]="activeTab !== i"
                [class.dark:text-muted-dark]="activeTab !== i"
                [class.dark:border-border-dark/80]="activeTab !== i"
                (click)="activeTab = i"
                role="tab"
                [attr.aria-selected]="activeTab === i"
                [attr.aria-controls]="'flash-panel'"
                [id]="'flash-tab-' + i"
              >
                {{ tab }}
              </button>
            }
          </div>
          <div class="flex gap-3 sm:gap-4 overflow-x-auto pb-2 -mx-1 px-1 scrollbar-none" id="flash-panel" role="tabpanel" aria-label="Flash sale items">
            @for (item of filteredItems; track item.skuId) {
              <div class="min-w-[155px] sm:min-w-[175px] bg-surface-card dark:bg-surface-card-dark rounded-xl border border-border/60 dark:border-border-dark/60 p-3 flex flex-col items-center text-center shrink-0 hover:shadow-md hover:-translate-y-0.5 hover:border-primary/30 dark:hover:border-primary/40 transition-all duration-300">
                <div class="relative w-24 h-24 sm:w-28 sm:h-28 rounded-lg bg-bg dark:bg-bg-dark flex items-center justify-center mb-2.5 overflow-hidden">
                  <img [src]="item.imageUrl" loading="lazy" appFallbackImage alt="{{ item.name }}" class="w-full h-full object-contain p-2" />
                  <span class="absolute top-1 left-1 bg-error text-white text-[10px] font-bold px-1.5 py-0.5 rounded">-{{ item.discountPercent }}%</span>
                </div>
                <p class="text-xs line-clamp-2 mb-2 flex-1 leading-snug">{{ item.name }}</p>
                <p class="text-primary font-bold text-sm">{{ item.salePrice | currencyFormat }}</p>
                <p class="text-muted dark:text-muted-dark text-xs line-through mb-2">{{ item.originalPrice | currencyFormat }}</p>
                <nz-progress [nzPercent]="soldPercent(item)" nzSize="small" [nzStrokeColor]="'var(--color-primary)'" [nzShowInfo]="false" class="w-full [&_.ant-progress-bg]:!rounded-full"></nz-progress>
                <p class="text-muted dark:text-muted-dark text-[11px] mt-1">Đã bán {{ item.total - item.remaining }}/{{ item.total }}</p>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
})
export class FlashSaleSectionComponent {
  private mock = inject(MockDataService);

  campaign = this.mock.getFlashSale();
  countdown = '03:15:42';
  flashCategories = ['Tất cả', 'Laptop', 'Linh kiện', 'Phụ kiện', 'Màn hình'];
  activeTab = 0;

  get filteredItems() {
    const items = this.campaign.items;
    if (this.activeTab === 0) return items;
    const catMap: Record<number, string> = { 1: 'Laptop', 2: 'Linh kiện', 3: 'Phụ kiện', 4: 'Màn hình' };
    const cat = catMap[this.activeTab];
    return items.filter(i => i.category === cat);
  }

  soldPercent(item: { remaining: number; total: number }): number {
    return Math.round(((item.total - item.remaining) / item.total) * 100);
  }
}
