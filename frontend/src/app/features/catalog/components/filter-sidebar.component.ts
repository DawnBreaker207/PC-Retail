import { Component, input, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
export interface FilterState {
  q: string;
  category: string;
  brands: string[];
  minPrice: number;
  maxPrice: number;
  sort: string;
  page: number;
  size: number;
}

export interface ActiveFilter {
  label: string;
  type: 'q' | 'category' | 'brand' | 'price';
  value: string;
}

export const PRICE_RANGES = [
  { label: 'Dưới 10 triệu', min: 0, max: 10_000_000 },
  { label: '10 - 20 triệu', min: 10_000_000, max: 20_000_000 },
  { label: '20 - 30 triệu', min: 20_000_000, max: 30_000_000 },
  { label: 'Trên 30 triệu', min: 30_000_000, max: 500_000_000 },
] as const;

@Component({
  selector: 'app-filter-sidebar',
  imports: [
    FormsModule, NzCheckboxModule, NzTagModule, NzButtonModule,
    NzIconModule, NzDrawerModule, NzCollapseModule,
  ],
  templateUrl: './filter-sidebar.component.html',
  styleUrls: ['./filter-sidebar.component.css'],
})
export class FilterSidebarComponent {
  filters = model.required<FilterState>();
  categories = input<{ slug: string; name: string }[]>([]);
  brands = input<string[]>([]);
  drawerOpen = model(false);

  filterChange = output<void>();
  clear = output<void>();

  priceRanges = PRICE_RANGES;

  get activeFilters(): ActiveFilter[] {
    const f = this.filters();
    const tags: ActiveFilter[] = [];
    if (f.q) tags.push({ label: `Tìm: "${f.q}"`, type: 'q', value: f.q });
    if (f.category) {
      const cat = this.categories().find(c => c.slug === f.category);
      tags.push({ label: cat?.name || f.category, type: 'category', value: f.category });
    }
    for (const b of f.brands) tags.push({ label: b, type: 'brand', value: b });
    if (f.minPrice > 0 || f.maxPrice < 500_000_000) {
      const matched = PRICE_RANGES.find(r => r.min === f.minPrice && r.max === f.maxPrice);
      tags.push({ label: matched?.label || `${(f.minPrice / 1000000).toFixed(0)}tr - ${(f.maxPrice / 1000000).toFixed(0)}tr`, type: 'price', value: '' });
    }
    return tags;
  }

  removeFilter(af: ActiveFilter) {
    this.filters.update(f => {
      switch (af.type) {
        case 'q': return { ...f, q: '' };
        case 'category': return { ...f, category: '' };
        case 'brand': return { ...f, brands: f.brands.filter(b => b !== af.value) };
        case 'price': return { ...f, minPrice: 0, maxPrice: 500_000_000 };
        default: return f;
      }
    });
    this.filterChange.emit();
  }

  onBrandChange(brand: string, checked: boolean) {
    const current = this.filters().brands;
    this.selectBrand(checked ? [...current, brand] : current.filter(b => b !== brand));
  }

  selectBrand(brands: string[]) {
    this.filters.update(f => ({ ...f, brands, page: 1 }));
    this.filterChange.emit();
  }

  selectPriceRange(min: number, max: number) {
    const isActive = this.filters().minPrice === min && this.filters().maxPrice === max;
    this.filters.update(f => ({
      ...f,
      minPrice: isActive ? 0 : min,
      maxPrice: isActive ? 500_000_000 : max,
      page: 1,
    }));
    this.filterChange.emit();
  }

  isPriceActive(min: number, max: number): boolean {
    return this.filters().minPrice === min && this.filters().maxPrice === max;
  }

  selectCategory(slug: string) {
    this.filters.update(f => ({
      ...f,
      category: f.category === slug ? '' : slug,
      page: 1,
    }));
    this.filterChange.emit();
  }

  clearAll() {
    this.clear.emit();
  }

  hasActive(): boolean {
    const f = this.filters();
    return !!(f.q || f.category || f.brands.length > 0 || f.minPrice > 0 || f.maxPrice < 500_000_000);
  }
}
