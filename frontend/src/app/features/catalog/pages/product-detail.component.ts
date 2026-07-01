import { Component, inject, signal, DestroyRef } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzRateModule } from 'ng-zorro-antd/rate';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku, Review } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';

@Component({
  selector: 'app-product-detail',
  imports: [
    RouterLink, FormsModule, DatePipe, NzButtonModule, NzIconModule, NzInputNumberModule, NzTagModule,
    NzRateModule, NzTabsModule, NzBreadCrumbModule, NzDividerModule, NzSpinModule,
    NzEmptyModule, CurrencyFormatPipe, ProductCardComponent, LoadingSpinnerComponent,
  ],
  template: `
    @if (loading) {
      <app-loading-spinner />
    } @else if (product(); as p) {
      <div class="max-w-screen-2xl mx-auto px-4 py-6 lg:py-8">
        <nz-breadcrumb class="mb-4">
          <nz-breadcrumb-item><a routerLink="/">Trang chủ</a></nz-breadcrumb-item>
          <nz-breadcrumb-item><a routerLink="/products">Sản phẩm</a></nz-breadcrumb-item>
          <nz-breadcrumb-item>{{ p.name }}</nz-breadcrumb-item>
        </nz-breadcrumb>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">
          <!-- Left: Images -->
          <div class="lg:col-span-5">
            <div class="relative aspect-square rounded-xl overflow-hidden bg-bg dark:bg-bg-dark border border-border/80 dark:border-border-dark/80 mb-3 shadow-sm">
              <img [src]="selectedImage()" [alt]="p.name" class="w-full h-full object-contain p-6 cursor-crosshair" />
              @if (p.originalPrice && p.originalPrice > p.price) {
                <span class="absolute top-3 left-3 bg-error text-white text-xs font-bold px-3 py-1 rounded-full shadow-sm">
                  -{{ discountPercent }}%
                </span>
              }
            </div>
            @if (p.imageUrls.length > 1) {
              <div class="flex gap-2 overflow-x-auto pb-2">
                @for (img of p.imageUrls; track img; let i = $index) {
                  <button
                    class="w-16 h-16 shrink-0 rounded-lg border-2 overflow-hidden transition-all"
                    [class.border-primary]="i === selectedIndex"
                    [class.border-border]="i !== selectedIndex"
                    (click)="selectImage(i)"
                  >
                    <img [src]="img" alt="{{ p.name }} thumbnail" class="w-full h-full object-contain p-1" />
                  </button>
                }
              </div>
            }
          </div>

          <!-- Right: Info -->
          <div class="lg:col-span-7">
            <p class="text-xs text-muted dark:text-muted-dark uppercase tracking-wider mb-1 font-medium">{{ p.brandName }}</p>
            <h1 class="text-xl lg:text-2xl font-bold mb-3 leading-tight">{{ p.name }}</h1>

            <div class="flex items-center gap-3 mb-4">
              <nz-rate [ngModel]="p.rating || 0" nzDisabled class="text-sm" />
              <span class="text-sm text-muted">({{ p.reviewCount || 0 }} đánh giá)</span>
              <span class="text-sm text-muted">| Đã bán {{ p.soldCount }}</span>
            </div>

            <div class="flex items-baseline gap-3 mb-5">
              <span class="text-2xl lg:text-3xl font-bold text-primary">{{ p.price | currencyFormat }}</span>
              @if (p.originalPrice && p.originalPrice > p.price) {
                <span class="text-base text-muted line-through">{{ p.originalPrice | currencyFormat }}</span>
                <nz-tag nzColor="error">-{{ discountPercent }}%</nz-tag>
              }
            </div>

            @if (p.stockAvailable !== undefined) {
              <p class="text-sm mb-4" [class.text-success]="p.stockAvailable > 0" [class.text-error]="p.stockAvailable === 0">
                @if (p.stockAvailable > 0) {
                  <span nz-icon nzType="check-circle" nzTheme="fill" class="mr-1"></span>
                  Còn hàng ({{ p.stockAvailable }} sản phẩm)
                } @else {
                  Hết hàng
                }
              </p>
            }

            <!-- Spec chips -->
            @if (p.specChips && p.specChips.length > 0) {
              <div class="bg-bg dark:bg-bg-dark rounded-xl p-4 mb-5 border border-border/60 dark:border-border-dark/60">
                <div class="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
                  @for (chip of p.specChips; track chip.label) {
                    <div class="flex items-center gap-2">
                      <span nz-icon [nzType]="chip.icon" class="text-primary text-sm"></span>
                      <span class="text-muted text-xs font-medium">{{ chip.label }}:</span>
                      <span class="font-medium text-xs">{{ chip.value }}</span>
                    </div>
                  }
                </div>
              </div>
            }

            <!-- Quantity + Buttons -->
            <div class="flex items-center gap-4 mb-5">
              <div class="flex items-center border border-border dark:border-border-dark rounded-lg overflow-hidden">
                <button nz-button nzType="text" class="!w-9 !h-9 !rounded-none" (click)="decrementQty()" [disabled]="quantity <= 1">
                  <span nz-icon nzType="minus" class="text-xs"></span>
                </button>
                <span class="w-12 text-center text-sm font-medium select-none">{{ quantity }}</span>
                <button nz-button nzType="text" class="!w-9 !h-9 !rounded-none" (click)="incrementQty()" [disabled]="p.stockAvailable !== undefined && quantity >= p.stockAvailable">
                  <span nz-icon nzType="plus" class="text-xs"></span>
                </button>
              </div>
            </div>

            <div class="flex flex-col sm:flex-row gap-3 mb-6">
              <button
                nz-button
                nzType="primary"
                nzSize="large"
                class="!rounded-lg !px-8 !h-12 !text-base !font-bold flex-1 sm:flex-none !shadow-sm hover:!shadow-md !transition-all"
                [disabled]="p.stockAvailable === 0"
                (click)="addToCart(p)"
              >
                <span nz-icon nzType="shopping-cart"></span>
                Thêm vào giỏ
              </button>
              <button
                nz-button
                nzSize="large"
                class="!rounded-lg !px-8 !h-12 !text-base !font-bold flex-1 sm:flex-none !bg-primary !border-primary !text-white hover:!bg-primary-dark !shadow-sm hover:!shadow-md !transition-all"
                [disabled]="p.stockAvailable === 0"
              >
                Mua ngay
              </button>
              <button nz-button nzType="default" nzSize="large" class="!rounded-lg !w-12 !h-12">
                <span nz-icon nzType="heart" nzTheme="outline"></span>
              </button>
            </div>

            <!-- Trust signals -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 bg-bg dark:bg-bg-dark rounded-xl border border-border/60 dark:border-border-dark/60">
              <div class="flex items-center gap-2.5">
                <span nz-icon nzType="check-circle" nzTheme="fill" class="text-success text-lg"></span>
                <div>
                  <p class="text-[11px] font-semibold">Chính hãng</p>
                  <p class="text-[10px] text-muted">100%</p>
                </div>
              </div>
              <div class="flex items-center gap-2.5">
                <span nz-icon nzType="sync" nzTheme="fill" class="text-primary text-lg"></span>
                <div>
                  <p class="text-[11px] font-semibold">Đổi trả</p>
                  <p class="text-[10px] text-muted">30 ngày</p>
                </div>
              </div>
              <div class="flex items-center gap-2.5">
                <span nz-icon nzType="car" nzTheme="fill" class="text-warning text-lg"></span>
                <div>
                  <p class="text-[11px] font-semibold">Miễn phí ship</p>
                  <p class="text-[10px] text-muted">Toàn quốc</p>
                </div>
              </div>
              <div class="flex items-center gap-2.5">
                <span nz-icon nzType="security-scan" nzTheme="fill" class="text-primary text-lg"></span>
                <div>
                  <p class="text-[11px] font-semibold">Bảo hành</p>
                  <p class="text-[10px] text-muted">24 tháng</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <nz-divider class="my-8 lg:my-10" />

        <div class="max-w-4xl">
          <nz-tabset>
            <nz-tab nzTitle="Mô tả sản phẩm">
              <div class="prose dark:prose-invert max-w-none py-4">
                <p>{{ p.description || 'Chưa có mô tả cho sản phẩm này.' }}</p>
              </div>
            </nz-tab>
            <nz-tab nzTitle="Thông số kỹ thuật">
              @if (p.specs || (p.specChips && p.specChips.length > 0)) {
                <table class="w-full text-sm mt-4">
                  <tbody>
                    @for (spec of getSpecEntries(); track spec.key) {
                      <tr class="border-b border-border/60 dark:border-border-dark/60">
                        <td class="py-3 pr-4 text-muted w-48 font-medium">{{ spec.key }}</td>
                        <td class="py-3">{{ spec.value }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              } @else {
                <p class="text-muted py-4">Chưa có thông số kỹ thuật.</p>
              }
            </nz-tab>
            <nz-tab [nzTitle]="'Đánh giá (' + (p.reviewCount || 0) + ')'">
              @if (reviews.length > 0) {
                <div class="space-y-4 mt-4">
                  @for (review of reviews; track review.id) {
                    <div class="border-b border-border/60 dark:border-border-dark/60 pb-4">
                      <div class="flex items-center gap-2 mb-2">
                        <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary">
                          {{ review.fullName.charAt(0) }}
                        </div>
                        <span class="font-medium text-sm">{{ review.fullName }}</span>
                        <nz-rate [ngModel]="review.rating" nzDisabled class="text-xs" />
                      </div>
                      <p class="text-sm text-muted">{{ review.comment }}</p>
                      <p class="text-xs text-muted/60 mt-1">{{ review.createdAt | date:'dd/MM/yyyy' }}</p>
                    </div>
                  }
                </div>
              } @else {
                <nz-empty nzNotFoundContent="Chưa có đánh giá nào"></nz-empty>
              }
            </nz-tab>
          </nz-tabset>
        </div>

        @if (relatedProducts.length > 0) {
          <nz-divider class="my-8 lg:my-10" />
          <h2 class="text-lg lg:text-xl font-bold mb-5">Sản phẩm liên quan</h2>
          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 sm:gap-4">
            @for (rp of relatedProducts; track rp.id) {
              <app-product-card [product]="rp" (addToCart)="cartStore.addItem(rp.id, 1)" />
            }
          </div>
        }
      </div>
    }
  `,
})
export class ProductDetailComponent {
  private route = inject(ActivatedRoute);
  private mock = inject(MockDataService);
  private destroyRef = inject(DestroyRef);
  cartStore = inject(CartStore);

  product = signal<Sku | null>(null);
  reviews: Review[] = [];
  relatedProducts: Sku[] = [];
  loading = true;
  quantity = 1;
  selectedIndex = 0;
  selectedImage = signal('');

  get discountPercent(): number {
    const p = this.product();
    if (!p?.originalPrice || p.originalPrice <= p.price) return 0;
    return Math.round((1 - p.price / p.originalPrice) * 100);
  }

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const slug = params.get('slug');
      if (slug) this.loadProduct(slug);
    });
  }

  private loadProduct(slug: string) {
    this.loading = true;
    this.mock.getProduct(slug).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (p) => {
        if (p) {
          this.product.set(p);
          this.selectedImage.set(p.imageUrls[0] || '');
          this.loadReviews();
          this.loadRelated(slug);
        }
        this.loading = false;
      },
      error: () => this.loading = false,
    });
  }

  private loadReviews() {
    this.mock.getReviews().pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((r) => this.reviews = r);
  }

  private loadRelated(slug: string) {
    this.mock.getRelatedProducts(slug).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((r) => this.relatedProducts = r);
  }

  selectImage(index: number) {
    this.selectedIndex = index;
    const p = this.product();
    if (p?.imageUrls[index]) this.selectedImage.set(p.imageUrls[index]);
  }

  decrementQty() {
    if (this.quantity > 1) this.quantity--;
  }

  incrementQty() {
    const p = this.product();
    if (p?.stockAvailable && this.quantity < p.stockAvailable) this.quantity++;
  }

  addToCart(p: Sku) {
    this.cartStore.addItem(p.id, this.quantity);
  }

  getSpecEntries(): { key: string; value: string }[] {
    const p = this.product();
    if (p?.specChips && p.specChips.length > 0) {
      return p.specChips.map(chip => ({ key: chip.label, value: chip.value }));
    }
    if (p?.specs) {
      return Object.entries(p.specs).map(([key, value]) => ({ key, value }));
    }
    return [];
  }
}
