import { Component, computed, input, model, output, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, X, Trash2 } from 'lucide-angular';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmSelectDirective } from '@shared/ui/select';

export interface FilterState {
  q: string;
  category: string;
  brands: string[];
  minPrice: number;
  maxPrice: number;
  specs: Record<string, string[]>;
  sort: string;
  page: number;
  size: number;
}

export interface SpecFilterOption {
  label: string;
  values: string[];
}

export interface ActiveFilter {
  label: string;
  type: 'q' | 'category' | 'brand' | 'price' | 'spec';
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
  standalone: true,
  imports: [FormsModule, LucideAngularModule, HlmBadgeDirective, HlmButtonDirective, HlmSelectDirective],
  templateUrl: './filter-sidebar.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./filter-sidebar.component.css'],
})
export class FilterSidebarComponent {
  filters = model.required<FilterState>();
  categories = input<{ slug: string; name: string }[]>([]);
  brands = input<string[]>([]);
  specOptions = input<SpecFilterOption[]>([]);

  filterChange = output<void>();
  clear = output<void>();

  readonly XIcon = X;
  readonly Trash2Icon = Trash2;
  readonly priceRanges = PRICE_RANGES;

  selectedPriceLabel = computed(() => {
    const f = this.filters();
    const matched = PRICE_RANGES.find((r) => r.min === f.minPrice && r.max === f.maxPrice);
    return matched?.label ?? '';
  });

  get activeFilters(): ActiveFilter[] {
    const f = this.filters();
    const tags: ActiveFilter[] = [];
    if (f.q) tags.push({ label: `Tìm: "${f.q}"`, type: 'q', value: f.q });
    if (f.category) {
      const cat = this.categories().find((c) => c.slug === f.category);
      tags.push({ label: cat?.name || f.category, type: 'category', value: f.category });
    }
    for (const b of f.brands) tags.push({ label: b, type: 'brand', value: b });
    if (f.minPrice > 0 || f.maxPrice < 500_000_000) {
      const matched = PRICE_RANGES.find((r) => r.min === f.minPrice && r.max === f.maxPrice);
      tags.push({
        label:
          matched?.label ||
          `${(f.minPrice / 1000000).toFixed(0)}tr - ${(f.maxPrice / 1000000).toFixed(0)}tr`,
        type: 'price',
        value: '',
      });
    }
    for (const [key, values] of Object.entries(f.specs)) {
      for (const v of values)
        tags.push({ label: `${key}: ${v}`, type: 'spec', value: `${key}::${v}` });
    }
    return tags;
  }

  removeFilter(af: ActiveFilter) {
    this.filters.update((f) => {
      switch (af.type) {
        case 'q':
          return { ...f, q: '' };
        case 'category':
          return { ...f, category: '' };
        case 'brand':
          return { ...f, brands: f.brands.filter((b) => b !== af.value) };
        case 'price':
          return { ...f, minPrice: 0, maxPrice: 500_000_000 };
        case 'spec': {
          const [key, value] = af.value.split('::');
          const remaining = (f.specs[key] || []).filter((v) => v !== value);
          const specs = { ...f.specs };
          if (remaining.length > 0) specs[key] = remaining;
          else delete specs[key];
          return { ...f, specs };
        }
        default:
          return f;
      }
    });
    this.filterChange.emit();
  }

  selectBrand(event: Event) {
    const target = event.target as HTMLSelectElement;
    const value = target.value;
    if (!value) return;
    this.filters.update((f) => {
      if (f.brands.includes(value)) return f;
      return { ...f, brands: [...f.brands, value], page: 1 };
    });
    target.value = '';
    this.filterChange.emit();
  }

  onPriceSelect(event: Event) {
    const target = event.target as HTMLSelectElement;
    const label = target.value;
    if (!label) {
      this.filters.update((f) => ({ ...f, minPrice: 0, maxPrice: 500_000_000, page: 1 }));
    } else {
      const range = PRICE_RANGES.find((r) => r.label === label);
      if (range) {
        this.filters.update((f) => ({ ...f, minPrice: range.min, maxPrice: range.max, page: 1 }));
      }
    }
    this.filterChange.emit();
  }

  selectCategory(event: Event) {
    const target = event.target as HTMLSelectElement;
    const slug = target.value;
    this.filters.update((f) => ({
      ...f,
      category: slug || '',
      specs: {},
      page: 1,
    }));
    this.filterChange.emit();
  }

  selectSpec(label: string, event: Event) {
    const target = event.target as HTMLSelectElement;
    const value = target.value;
    if (!value) return;
    this.filters.update((f) => {
      const specs = { ...f.specs };
      const current = specs[label] || [];
      if (!current.includes(value)) {
        specs[label] = [...current, value];
      }
      return { ...f, specs, page: 1 };
    });
    target.value = '';
    this.filterChange.emit();
  }

  clearAll() {
    this.clear.emit();
  }

  hasActive(): boolean {
    const f = this.filters();
    return !!(
      f.q ||
      f.category ||
      f.brands.length > 0 ||
      f.minPrice > 0 ||
      f.maxPrice < 500_000_000 ||
      Object.values(f.specs).some((v) => v.length > 0)
    );
  }
}
