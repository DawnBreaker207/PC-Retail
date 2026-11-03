import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { MockDataService } from '@core/services/mock-data.service';
import { CartStore } from '@features/cart/store/cart.store';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, ProductCardComponent, EmptyStateComponent],
  templateUrl: './wishlist.component.html',
})
export class WishlistComponent {
  private mock = inject(MockDataService);
  private cartStore = inject(CartStore);
  wishlist = inject(WishlistStore);

  readonly ChevronRightIcon = ChevronRight;

  private allProducts = this.mock.generateProducts();

  products = computed(() =>
    this.allProducts.filter((p) => this.wishlist.has(p.id)),
  );

  addToCart(skuId: number) {
    this.cartStore.addItem(skuId, 1);
  }
}
