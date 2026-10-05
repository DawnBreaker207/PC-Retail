import { Component, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { Sku } from '@core/models/common.model';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';

@Component({
  selector: 'app-product-card',
  imports: [RouterLink, NzIconModule, CurrencyFormatPipe, FallbackImageDirective],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.css',
})
export class ProductCardComponent {
  wishlist = inject(WishlistStore);

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
