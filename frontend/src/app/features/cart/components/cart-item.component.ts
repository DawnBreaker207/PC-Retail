import { Component, inject, input, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Trash2, Minus, Plus } from 'lucide-angular';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartItem } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTooltipDirective } from '@shared/ui/tooltip';

@Component({
  selector: 'app-cart-item',
  standalone: true,
  imports: [RouterLink, FormsModule, LucideAngularModule, CurrencyFormatPipe, HlmButtonDirective, HlmTooltipDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './cart-item.component.html',
})
export class CartItemComponent {
  private store = inject(CartStore);
  private destroyRef = inject(DestroyRef);
  item = input.required<CartItem>();

  readonly Trash2Icon = Trash2;
  readonly MinusIcon = Minus;
  readonly PlusIcon = Plus;

  private qtySubject = new Subject<number>();

  constructor() {
    this.qtySubject
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((qty) => {
        this.store.updateQuantity(this.item().skuId, qty);
      });
  }

  onQtyChange(qty: number) {
    if (qty < 1) return;
    this.qtySubject.next(qty);
  }

  decrementQty() {
    const current = this.item().quantity;
    if (current > 1) {
      this.onQtyChange(current - 1);
    }
  }

  incrementQty() {
    const current = this.item().quantity;
    if (current < this.item().stock) {
      this.onQtyChange(current + 1);
    }
  }

  toggleSelect() {
    this.store.toggleSelect(this.item().skuId);
  }

  remove() {
    this.store.removeItem(this.item().skuId);
  }
}
