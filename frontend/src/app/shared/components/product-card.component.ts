import { Component, computed, inject, input, output, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HlmCardDirective } from '@shared/ui/card';
import { LucideAngularModule, Heart, ShoppingCart, Zap, Star, Loader2 } from 'lucide-angular';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { Sku } from '@core/models/common.model';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [
    RouterLink,
    HlmCardDirective,
    LucideAngularModule,
    CurrencyFormatPipe,
    FallbackImageDirective,
  ],
  templateUrl: './product-card.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './product-card.component.css',
})
export class ProductCardComponent {
  wishlist = inject(WishlistStore);

  product = input.required<Sku>();
  badge = input<'flash' | 'discount' | null>(null);
  isAdding = input(false);

  cardClick = output();
  addToCart = output();

  readonly HeartIcon = Heart;
  readonly CartIcon = ShoppingCart;
  readonly ZapIcon = Zap;
  readonly StarIcon = Star;
  readonly LoaderIcon = Loader2;

  discountPercent = computed(() => {
    const p = this.product();
    if (!p.originalPrice || p.originalPrice <= p.price) return 0;
    return Math.round((1 - p.price / p.originalPrice) * 100);
  });
}
