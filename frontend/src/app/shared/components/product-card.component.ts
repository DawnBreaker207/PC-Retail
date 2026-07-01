import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { Sku } from '@core/models/common.model';

@Component({
  selector: 'app-product-card',
  imports: [RouterLink, NzButtonModule, NzIconModule, CurrencyFormatPipe],
  template: `
    <div
      class="group bg-surface dark:bg-surface-dark rounded-card border border-border/80 dark:border-border-dark/80 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 overflow-hidden flex flex-col cursor-pointer"
      (click)="cardClick.emit()"
      (keyup.enter)="cardClick.emit()"
      tabindex="0"
      role="button"
    >
      <div class="relative aspect-square bg-bg dark:bg-bg-dark overflow-hidden">
        <img
          [src]="product().imageUrls[0]"
          [alt]="product().name"
          class="w-full h-full object-contain p-4 sm:p-6 group-hover:scale-110 transition-transform duration-500"
          loading="lazy"
        />
        @if (badge()) {
          <span
            class="absolute top-2 left-2 text-[10px] font-bold px-2.5 py-1 rounded-md z-10 shadow-sm text-white"
            [class.bg-primary]="badge() === 'flash'"
            [class.bg-warning]="badge() === 'discount'"
          >
            @if (badge() === 'flash') { FLASH SALE }
            @if (badge() === 'discount') {
              -{{ discountPercent }}%
            }
          </span>
        }
        <button
          nz-button
          nzType="text"
          class="!absolute !top-2 !right-2 !w-8 !h-8 !rounded-full !bg-white/80 dark:!bg-slate-800/80 !opacity-0 group-hover:!opacity-100 !transition-opacity !duration-200 !shadow-sm"
          (click)="$event.stopPropagation()"
        >
          <span nz-icon nzType="heart" nzTheme="outline" class="text-muted dark:text-muted-dark"></span>
        </button>
      </div>

      <div class="p-3 sm:p-4 flex-1 flex flex-col">
        <p class="text-[11px] text-muted dark:text-muted-dark mb-1 uppercase tracking-wider font-medium">{{ product().brandName }}</p>
        <a
          [routerLink]="['/products', product().slug]"
          class="text-sm font-medium line-clamp-2 hover:text-primary transition-colors duration-200 mb-2 flex-1 leading-snug"
          (click)="$event.stopPropagation()"
        >
          {{ product().name }}
        </a>

        @if (product().specChips && product().specChips!.length > 0) {
          <div class="flex flex-wrap gap-1 mb-2">
            @for (chip of product().specChips!.slice(0, 4); track chip.label) {
              <span class="inline-flex items-center gap-0.5 text-[10px] bg-bg dark:bg-bg-dark text-muted dark:text-muted-dark px-1.5 py-0.5 rounded border border-border/60 dark:border-border-dark/60">
                <span nz-icon [nzType]="chip.icon" class="text-[9px]"></span>
                <span>{{ chip.value }}</span>
              </span>
            }
          </div>
        }

        <div class="flex items-baseline gap-2 flex-wrap">
          <span class="text-primary font-bold text-base sm:text-lg tracking-tight">{{ product().price | currencyFormat }}</span>
          @if (discountPercent > 0) {
            <span class="text-xs text-muted dark:text-muted-dark line-through">{{ product().originalPrice! | currencyFormat }}</span>
          }
        </div>

        @if (product().soldCount > 0 || product().rating) {
          <div class="flex items-center gap-2 mt-0.5">
            @if (product().rating) {
              <div class="flex items-center text-warning text-[11px]">
                <span nz-icon nzType="star" nzTheme="fill"></span>
                <span class="ml-0.5 text-muted dark:text-muted-dark font-medium">{{ product().rating }}</span>
              </div>
              @if (product().reviewCount) {
                <span class="text-[11px] text-muted dark:text-muted-dark">({{ product().reviewCount }})</span>
              }
            }
            @if (product().soldCount > 0) {
              <span class="text-[11px] text-muted dark:text-muted-dark ml-auto">Đã bán {{ product().soldCount }}</span>
            }
          </div>
        }

        <button
          nz-button
          class="!mt-2.5 !w-full !rounded-lg !border-primary !text-primary hover:!bg-primary hover:!text-white !transition-all !duration-200 !text-xs sm:!text-sm !font-semibold !h-9"
          [class.!bg-primary]="isAdding()"
          [class.!text-white]="isAdding()"
          (click)="$event.stopPropagation(); addToCart.emit()"
        >
          @if (isAdding()) {
            <span nz-icon nzType="loading" nzTheme="outline" class="mr-1"></span>
          }
          + Thêm vào giỏ
        </button>
      </div>
    </div>
  `,
})
export class ProductCardComponent {
  product = input.required<Sku>();
  badge = input<'flash' | 'discount' | null>(null);
  isAdding = input(false);

  cardClick = output();
  addToCart = output();

  get discountPercent(): number {
    const p = this.product();
    if (!p.originalPrice || p.originalPrice <= p.price) return 0;
    return Math.round((1 - p.price / p.originalPrice) * 100);
  }
}
