import { Component, inject, input, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartItem } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-cart-item',
  imports: [
    RouterLink,
    FormsModule,
    NzCheckboxModule,
    NzInputNumberModule,
    NzButtonModule,
    NzPopconfirmModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './cart-item.component.html',
})
export class CartItemComponent {
  private store = inject(CartStore);
  private destroyRef = inject(DestroyRef);
  item = input.required<CartItem>();

  private qtySubject = new Subject<number>();

  constructor() {
    this.qtySubject
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((qty) => {
        this.store.updateQuantity(this.item().skuId, qty);
      });
  }

  onQtyChange(qty: number) {
    this.qtySubject.next(qty);
  }

  toggleSelect() {
    this.store.toggleSelect(this.item().skuId);
  }

  remove() {
    this.store.removeItem(this.item().skuId);
  }
}
