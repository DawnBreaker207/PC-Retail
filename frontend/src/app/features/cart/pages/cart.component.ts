import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartStore } from '@features/cart/store/cart.store';
import { CartItemComponent } from '../components/cart-item.component';
import { OrderSummaryComponent } from '../components/order-summary.component';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-cart',
  imports: [
    RouterLink,
    CartItemComponent,
    OrderSummaryComponent,
    NzButtonModule,
    NzCheckboxModule,
    NzSpinModule,
    NzIconModule,
  ],
  templateUrl: './cart.component.html',
})
export class CartComponent {
  readonly store = inject(CartStore);

  get items() { return this.store.items; }
  get isLoading() { return this.store.isLoading; }

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
