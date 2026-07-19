import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
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
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
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
    NzEmptyModule, CurrencyFormatPipe, FallbackImageDirective, ProductCardComponent, LoadingSpinnerComponent,
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css',
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

  discountPercent = computed(() => {
    const p = this.product();
    if (!p?.originalPrice || p.originalPrice <= p.price) return 0;
    return Math.round((1 - p.price / p.originalPrice) * 100);
  });

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

  specEntries = computed<{ key: string; value: string }[]>(() => {
    const p = this.product();
    if (p?.specChips && p.specChips.length > 0) {
      return p.specChips.map(chip => ({ key: chip.label, value: chip.value }));
    }
    if (p?.specs) {
      return Object.entries(p.specs).map(([key, value]) => ({ key, value }));
    }
    return [];
  });
}
