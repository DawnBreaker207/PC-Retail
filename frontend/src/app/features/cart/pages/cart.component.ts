import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ShoppingCart } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { CartStore } from '@features/cart/store/cart.store';
import { CartItemComponent } from '../components/cart-item.component';
import { OrderSummaryComponent } from '../components/order-summary.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    RouterLink,
    LucideAngularModule,
    HlmButtonDirective,
    LoadingSpinnerComponent,
    CartItemComponent,
    OrderSummaryComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './cart.component.html',
})
export class CartComponent {
  readonly store = inject(CartStore);
  readonly CartIcon = ShoppingCart;

  get items() {
    return this.store.items;
  }
  get isLoading() {
    return this.store.isLoading;
  }

  get allSelected(): boolean {
    return this.store.items().length > 0 && this.store.items().every((i) => i.selected);
  }

  toggleAll() {
    this.store.toggleSelectAll();
  }

  trackBySkuId(_index: number, item: { skuId: number }) {
    return item.skuId;
  }
}
