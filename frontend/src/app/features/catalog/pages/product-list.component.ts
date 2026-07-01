import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSliderModule } from 'ng-zorro-antd/slider';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { PageHeaderComponent } from '@shared/components/page-header.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';

interface Filters {
  q: string;
  category: string;
  brand: string;
  minPrice: number;
  maxPrice: number;
  sort: string;
  page: number;
  size: number;
}

const SORT_OPTIONS = [
  { value: 'newest', label: 'Mới nhất' },
  { value: 'price-asc', label: 'Giá: Thấp đến Cao' },
  { value: 'price-desc', label: 'Giá: Cao đến Thấp' },
  { value: 'sold', label: 'Bán chạy' },
];

@Component({
  selector: 'app-product-list',
  imports: [
    RouterLink,
    NzButtonModule, NzInputModule, NzSelectModule, NzIconModule,
    NzPaginationModule, NzSliderModule, NzCheckboxModule, NzCollapseModule,
    NzSpinModule, NzEmptyModule, NzBreadCrumbModule,
    FormsModule, CurrencyFormatPipe, ProductCardComponent, PageHeaderComponent, EmptyStateComponent, LoadingSpinnerComponent,
  ],
  template: `
    <div class="max-w-screen-2xl mx-auto px-4 py-6 lg:py-8">
      <nz-breadcrumb class="mb-4">
        <nz-breadcrumb-item><a routerLink="/">Trang chủ</a></nz-breadcrumb-item>
        <nz-breadcrumb-item>{{ headerTitle() }}</nz-breadcrumb-item>
      </nz-breadcrumb>

      <app-page-header
        [title]="headerTitle()"
        description="Khám phá bộ sưu tập sản phẩm PC & Laptop chính hãng"
      />

      <div class="flex gap-6 mt-6">
        <aside class="hidden lg:block w-64 shrink-0">
          <div class="sticky top-24 space-y-4">
            <nz-collapse nzAccordion>
              <nz-collapse-panel nzHeader="Danh mục" [nzActive]="true">
                <div class="space-y-2">
                  @for (cat of categories; track cat.slug) {
                    <button
                      class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                      [class.bg-primary-light]="filters().category === cat.slug"
                      [class.text-primary]="filters().category === cat.slug"
                      [class.text-muted]="filters().category !== cat.slug"
                      [class.hover-bg-surface-alt]="filters().category !== cat.slug"
                      (click)="setFilter('category', cat.slug)"
                    >
                      {{ cat.name }}
                    </button>
                  }
                </div>
              </nz-collapse-panel>
              <nz-collapse-panel nzHeader="Khoảng giá" [nzActive]="true">
                <div class="px-2">
                  <nz-slider
                    nzRange
                    [nzMin]="0"
                    [nzMax]="100000000"
                    [nzStep]="1000000"
                    [nzTipFormatter]="formatPrice"
                    [ngModel]="[filters().minPrice, filters().maxPrice]"
                    (ngModelChange)="onPriceChange($event)"
                  />
                  <div class="flex justify-between text-xs text-muted mt-1">
                    <span>{{ filters().minPrice | currencyFormat }}</span>
                    <span>{{ filters().maxPrice | currencyFormat }}</span>
                  </div>
                </div>
              </nz-collapse-panel>
              <nz-collapse-panel nzHeader="Thương hiệu" [nzActive]="true">
                <div class="space-y-2">
                  @for (brand of brands; track brand) {
                    <label
                      nz-checkbox
                      [nzId]="'brand-' + brand"
                      [for]="'brand-' + brand"
                      [nzChecked]="filters().brand === brand"
                      (nzCheckedChange)="setFilter('brand', $event ? brand : '')"
                      class="block text-sm"
                    >
                      {{ brand }}
                    </label>
                  }
                </div>
              </nz-collapse-panel>
            </nz-collapse>
          </div>
        </aside>

        <div class="flex-1 min-w-0">
          <div class="flex flex-wrap items-center gap-3 mb-5">
            <nz-input-group nzPrefixIcon="search" class="max-w-sm">
              <input
                nz-input
                placeholder="Tìm kiếm sản phẩm..."
                [ngModel]="filters().q"
                (ngModelChange)="onSearch($event)"
                class="!rounded-lg"
              />
            </nz-input-group>

            <nz-select
              [ngModel]="filters().sort"
              (ngModelChange)="setFilter('sort', $event)"
              nzPlaceHolder="Sắp xếp"
              class="min-w-[160px]"
            >
              @for (opt of sortOptions; track opt.value) {
                <nz-option [nzValue]="opt.value" [nzLabel]="opt.label" />
              }
            </nz-select>

            <button
              nz-button
              class="lg:!hidden"
              nzType="default"
              (click)="showMobileFilters.set(true)"
            >
              <span nz-icon nzType="filter" nzTheme="outline"></span>
              Bộ lọc
            </button>

            @if (hasActiveFilters()) {
              <button nz-button nzType="link" class="text-sm" (click)="clearFilters()">
                <span nz-icon nzType="close"></span>
                Xóa bộ lọc
              </button>
            }

            <span class="text-sm text-muted ml-auto">{{ totalElements }} sản phẩm</span>
          </div>

          @if (isLoading) {
            <app-loading-spinner />
          } @else if (products.length === 0) {
            <app-empty-state
              icon="search-off"
              title="Không tìm thấy sản phẩm"
              description="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"
            />
          } @else {
            <div class="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-5 gap-3 sm:gap-4">
              @for (product of products; track product.id) {
                <app-product-card
                  [product]="product"
                  [badge]="getBadge(product)"
                  (addToCart)="addToCart(product)"
                />
              }
            </div>

            @if (totalPages > 1) {
              <div class="flex justify-center mt-8">
                <nz-pagination
                  [nzPageIndex]="filters().page"
                  [nzTotal]="totalElements"
                  [nzPageSize]="filters().size"
                  [nzShowSizeChanger]="false"
                  (nzPageIndexChange)="setFilter('page', $event)"
                />
              </div>
            }
          }
        </div>
      </div>
    </div>

    @if (showMobileFilters()) {
      <div class="fixed inset-0 z-50 lg:hidden" (click)="showMobileFilters.set(false)" (keydown.enter)="showMobileFilters.set(false)" tabindex="0">
        <div class="absolute inset-0 bg-black/50"></div>
        <div class="absolute left-0 top-0 bottom-0 w-72 bg-surface dark:bg-surface-dark p-5 overflow-y-auto" (click)="$event.stopPropagation()" (keydown.enter)="$event.stopPropagation()" tabindex="0">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-bold text-lg">Bộ lọc</h3>
            <button nz-button nzType="text" (click)="showMobileFilters.set(false)">
              <span nz-icon nzType="close"></span>
            </button>
          </div>
          <nz-collapse nzAccordion>
            <nz-collapse-panel nzHeader="Danh mục" [nzActive]="true">
              @for (cat of categories; track cat.slug) {
                <button class="w-full text-left px-3 py-2 rounded-lg text-sm"
                  [class.bg-primary-light]="filters().category === cat.slug"
                  [class.text-primary]="filters().category === cat.slug"
                  [class.text-muted]="filters().category !== cat.slug"
                  (click)="showMobileFilters.set(false); setFilter('category', cat.slug)">
                  {{ cat.name }}
                </button>
              }
            </nz-collapse-panel>
            <nz-collapse-panel nzHeader="Thương hiệu" [nzActive]="true">
              @for (brand of brands; track brand) {
                <label nz-checkbox [nzId]="'m-brand-' + brand" [for]="'m-brand-' + brand"
                  [nzChecked]="filters().brand === brand"
                  (nzCheckedChange)="showMobileFilters.set(false); setFilter('brand', $event ? brand : '')" class="block text-sm">
                  {{ brand }}
                </label>
              }
            </nz-collapse-panel>
          </nz-collapse>
        </div>
      </div>
    }
  `,
})
export class ProductListComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private mock = inject(MockDataService);
  private cartStore = inject(CartStore);
  private destroyRef = inject(DestroyRef);

  products: Sku[] = [];
  totalElements = 0;
  totalPages = 0;
  isLoading = true;
  showMobileFilters = signal(false);
  categories = this.mock.categories;
  brands = this.mock.brands.map(b => b.name);
  sortOptions = SORT_OPTIONS;

  filters = signal<Filters>({
    q: '', category: '', brand: '', minPrice: 0, maxPrice: 100_000_000,
    sort: 'newest', page: 1, size: 20,
  });

  constructor() {
    this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const f = this.filters();
      this.filters.set({
        q: params['q'] || f.q,
        category: params['category'] || f.category,
        brand: params['brand'] || f.brand,
        minPrice: params['minPrice'] ? Number(params['minPrice']) : f.minPrice,
        maxPrice: params['maxPrice'] ? Number(params['maxPrice']) : f.maxPrice,
        sort: params['sort'] || f.sort,
        page: params['page'] ? Number(params['page']) : 1,
        size: f.size,
      });
      this.loadProducts();
    });
  }

  private loadProducts() {
    this.isLoading = true;
    const f = this.filters();
    const params: Record<string, string> = {};
    if (f.q) params['q'] = f.q;
    if (f.category) params['category'] = f.category;
    if (f.brand) params['brand'] = f.brand;
    if (f.minPrice > 0) params['minPrice'] = String(f.minPrice);
    if (f.maxPrice < 100_000_000) params['maxPrice'] = String(f.maxPrice);
    if (f.sort) params['sort'] = f.sort;
    params['page'] = String(f.page - 1);
    params['size'] = String(f.size);

    this.mock.getProducts(params).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (res) => {
        this.products = res.content;
        this.totalElements = res.pagination.totalElements;
        this.totalPages = Math.ceil(res.pagination.totalElements / res.pagination.pageSize);
        this.isLoading = false;
      },
      error: () => {
        this.products = [];
        this.totalElements = 0;
        this.isLoading = false;
      },
    });
  }

  private updateUrl() {
    const f = this.filters();
    const qp: Record<string, string | number> = {};
    if (f.q) qp['q'] = f.q;
    if (f.category) qp['category'] = f.category;
    if (f.brand) qp['brand'] = f.brand;
    if (f.minPrice > 0) qp['minPrice'] = f.minPrice;
    if (f.maxPrice < 100_000_000) qp['maxPrice'] = f.maxPrice;
    if (f.sort && f.sort !== 'newest') qp['sort'] = f.sort;
    if (f.page > 1) qp['page'] = f.page;
    this.router.navigate([], { queryParams: qp, replaceUrl: true });
  }

  setFilter(key: keyof Filters, value: unknown) {
    this.filters.update((f) => ({ ...f, [key]: value, page: key === 'page' ? (value as number) : 1 }));
    this.updateUrl();
  }

  onSearch(value: string) {
    this.setFilter('q', value);
  }

  onPriceChange(value: [number, number]) {
    this.filters.update((f) => ({ ...f, minPrice: value[0], maxPrice: value[1], page: 1 }));
    this.updateUrl();
  }

  hasActiveFilters(): boolean {
    const f = this.filters();
    return !!(f.q || f.category || f.brand || f.minPrice > 0 || f.maxPrice < 100_000_000 || f.page > 1);
  }

  clearFilters() {
    this.filters.set({ q: '', category: '', brand: '', minPrice: 0, maxPrice: 100_000_000, sort: 'newest', page: 1, size: 20 });
    this.router.navigate([], { replaceUrl: true });
  }

  headerTitle(): string {
    const cat = this.filters().category;
    return cat ? this.categories.find((c) => c.slug === cat)?.name || 'Sản phẩm' : 'Sản phẩm';
  }

  getBadge(p: Sku): 'flash' | 'discount' | null {
    if (p.originalPrice && p.originalPrice > p.price) return 'discount';
    return null;
  }

  addToCart(product: Sku) {
    this.cartStore.addItem(product.id, 1);
  }

  formatPrice(value: number): string {
    return value.toLocaleString('vi-VN') + '₫';
  }
}
