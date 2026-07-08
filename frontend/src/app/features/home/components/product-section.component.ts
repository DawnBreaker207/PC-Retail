import { Component, inject, input, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MockDataService } from '@core/services/mock-data.service';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { CartStore } from '@features/cart/store/cart.store';
import { HomeSection, Sku } from '@core/models/common.model';

@Component({
  selector: 'app-product-section',
  imports: [RouterLink, ProductCardComponent],
  template: `
    <div class="max-w-screen-2xl mx-auto px-4 mt-8 lg:mt-10">
      <div class="flex items-center justify-between mb-5">
        <h2 class="text-lg lg:text-xl font-bold tracking-tight">{{ section().title }}</h2>
        <a [routerLink]="section().link" class="text-sm text-muted dark:text-muted-dark hover:text-primary font-medium transition-colors">
          Xem tất cả →
        </a>
      </div>

      @if (section().subNav && section().subNav.length > 0) {
        <div class="flex flex-wrap gap-1.5 mb-5" role="tablist" [attr.aria-label]="'Danh mục ' + section().title">
          @for (nav of section().subNav; track nav.slug; let i = $index) {
            <button
              class="min-h-11 px-4 text-xs font-medium rounded-full border transition-all duration-200 focus-visible:outline-2 focus-visible:outline-primary/60 focus-visible:outline-offset-1"
              [class.bg-surface-card]="activeSubNav() !== nav.slug"
              [class.text-muted]="activeSubNav() !== nav.slug"
              [class.border-border/80]="activeSubNav() !== nav.slug"
              [class.dark:bg-surface-card-dark]="activeSubNav() !== nav.slug"
              [class.dark:text-muted-dark]="activeSubNav() !== nav.slug"
              [class.dark:border-border-dark/80]="activeSubNav() !== nav.slug"
              [class.bg-primary]="activeSubNav() === nav.slug"
              [class.text-white]="activeSubNav() === nav.slug"
              [class.border-primary]="activeSubNav() === nav.slug"
              (click)="filterBy(nav.slug)"
              role="tab"
              [attr.aria-selected]="activeSubNav() === nav.slug"
              [attr.aria-controls]="'section-panel-' + section().title"
              [id]="'section-tab-' + section().title + '-' + i"
            >
              {{ nav.label }}
            </button>
          }
        </div>
      }

      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 sm:gap-4"
           role="tabpanel"
           [attr.aria-label]="'Sản phẩm ' + section().title"
           [id]="'section-panel-' + section().title">
        @for (product of filteredProducts(); track product.id) {
          <app-product-card
            [product]="product"
            [badge]="product.originalPrice && product.originalPrice > product.price ? 'discount' : null"
            (addToCart)="cartStore.addItem(product.id, 1)"
          />
        }
      </div>
    </div>
  `,
})
export class ProductSectionComponent implements OnInit {
  private mock = inject(MockDataService);
  readonly cartStore = inject(CartStore);

  section = input.required<HomeSection>();

  activeSubNav = signal('');
  filteredProducts = signal<Sku[]>([]);

  ngOnInit() {
    this.filteredProducts.set(this.mock.getSectionProducts());
  }

  filterBy(slug: string) {
    this.activeSubNav.set(slug);
    this.filteredProducts.set(this.mock.getSectionProducts(slug));
  }
}
