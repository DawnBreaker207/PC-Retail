import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';
import { FilterSidebarComponent, FilterState } from '../components/filter-sidebar.component';

const SORT_OPTIONS = [
  { value: 'sold', label: 'Bán chạy' },
  { value: 'newest', label: 'Mới nhất' },
  { value: 'price-asc', label: 'Giá: Thấp đến Cao' },
  { value: 'price-desc', label: 'Giá: Cao đến Thấp' },
];

function defaultFilters(): FilterState {
  return {
    q: '', category: '', brands: [], minPrice: 0, maxPrice: 500_000_000,
    sort: 'sold', page: 1, size: 20,
  };
}

@Component({
  selector: 'app-product-list',
  imports: [
    RouterLink, FormsModule,
    NzButtonModule, NzSelectModule, NzIconModule,
    NzPaginationModule, NzSpinModule, NzEmptyModule,
    NzBreadCrumbModule, NzDrawerModule,
    ProductCardComponent, LoadingSpinnerComponent,
    FilterSidebarComponent,
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css'],
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
  drawerOpen = signal(false);

  headerTitle = computed(() => {
    const cat = this.filters().category;
    if (!cat) return 'Sản phẩm';
    return this.categories.find(c => c.slug === cat)?.name || 'Sản phẩm';
  });

  categories = this.mock.categories;
  brands = this.mock.brands.map(b => b.name);
  sortOptions = SORT_OPTIONS;

  filters = signal<FilterState>(defaultFilters());

  constructor() {
    this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const f = this.filters();
      this.filters.set({
        q: params['q'] || f.q,
        category: params['category'] || f.category,
        brands: params['brand'] ? (Array.isArray(params['brand']) ? params['brand'] : [params['brand']]) : f.brands,
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
    if (f.brands.length > 0) params['brand'] = f.brands.join(',');
    if (f.minPrice > 0) params['minPrice'] = String(f.minPrice);
    if (f.maxPrice < 500_000_000) params['maxPrice'] = String(f.maxPrice);
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
    const qp: Record<string, string | number | string[]> = {};
    if (f.q) qp['q'] = f.q;
    if (f.category) qp['category'] = f.category;
    if (f.brands.length > 0) qp['brand'] = f.brands;
    if (f.minPrice > 0) qp['minPrice'] = f.minPrice;
    if (f.maxPrice < 500_000_000) qp['maxPrice'] = f.maxPrice;
    if (f.sort && f.sort !== 'sold') qp['sort'] = f.sort;
    if (f.page > 1) qp['page'] = f.page;
    this.router.navigate([], { queryParams: qp, replaceUrl: true });
  }

  onFiltersChanged() {
    this.updateUrl();
  }

  onClearFilters() {
    this.filters.set(defaultFilters());
    this.router.navigate([], { replaceUrl: true });
  }

  onPageChange(page: number) {
    this.filters.update(f => ({ ...f, page }));
    this.onFiltersChanged();
  }

  setSort(sort: string) {
    this.filters.update(f => ({ ...f, sort, page: 1 }));
    this.onFiltersChanged();
  }

  addToCart(product: Sku) {
    this.cartStore.addItem(product.id, 1);
  }

  getBadge(p: Sku): 'flash' | 'discount' | null {
    if (p.originalPrice && p.originalPrice > p.price) return 'discount';
    return null;
  }
}
