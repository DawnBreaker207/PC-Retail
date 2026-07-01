import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartStore } from '@features/cart/store/cart.store';

@Component({
  selector: 'app-order-summary',
  imports: [RouterLink, NzButtonModule, CurrencyFormatPipe],
  templateUrl: './order-summary.component.html',
})
export class OrderSummaryComponent {
  readonly store = inject(CartStore);

  get totalAmount() { return this.store.totalAmount; }
  get selectedAmount() { return this.store.selectedAmount; }
  get selectedCount() { return this.store.selectedItems; }
}
