import { signalStore, withState, withComputed, withMethods, withHooks, patchState } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface CartState {
  items: CartItem[];
  isLoading: boolean;
  version: number | null;
}

const initialState: CartState = {
  items: [],
  isLoading: false,
  version: null,
};

export const CartStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed(({ items }) => ({
    totalCount: computed(() => items().length),
    totalAmount: computed(() => items().reduce((sum, i) => sum + i.price * i.quantity, 0)),
    selectedItems: computed(() => items().filter((i) => i.selected)),
    selectedAmount: computed(() =>
      items().filter((i) => i.selected).reduce((sum, i) => sum + i.price * i.quantity, 0),
    ),
  })),
  withMethods((store, http = inject(HttpClient)) => ({
    loadCart(): void {
      patchState(store, { isLoading: true });
      http.get<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.base).subscribe((res) => {
        patchState(store, {
          items: res.items.map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    },

    addItem(skuId: number, quantity: number): void {
      http.post(ApiEndpoints.cart.base + '/items', { skuId, quantity }).subscribe(() => this.loadCart());
    },

    updateQuantity(skuId: number, quantity: number): void {
      const ver = store.version();
      const body: Record<string, unknown> = { quantity };
      if (ver !== null) body['version'] = ver;
      http.put(ApiEndpoints.cart.item(skuId), body).subscribe(() => this.loadCart());
    },

    removeItem(skuId: number): void {
      http.delete(ApiEndpoints.cart.item(skuId)).subscribe(() => this.loadCart());
    },

    clearCart(): void {
      http.delete(ApiEndpoints.cart.base).subscribe(() => patchState(store, { items: [], version: null }));
    },

    toggleSelect(skuId: number): void {
      patchState(store, {
        items: store.items().map((i) => (i.skuId === skuId ? { ...i, selected: !i.selected } : i)),
      });
    },

    toggleSelectAll(): void {
      const allSelected = store.items().every((i) => i.selected);
      patchState(store, {
        items: store.items().map((i) => ({ ...i, selected: !allSelected })),
      });
    },
  })),
  withHooks({
    onInit(store) {
      store.loadCart();
    },
  }),
);
