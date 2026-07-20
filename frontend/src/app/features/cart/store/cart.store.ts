import { signalStore, withState, withComputed, withMethods, withHooks, patchState } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface CartState {
  items: CartItem[];
  isLoading: boolean;
  error: string | null;
  version: number | null;
}

const initialState: CartState = {
  items: [],
  isLoading: false,
  error: null,
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
      patchState(store, { isLoading: true, error: null });
      http.get<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.base).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of({ items: [], version: undefined });
        }),
      ).subscribe((res) => {
        patchState(store, {
          items: res.items.map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    },

    addItem(skuId: number, quantity: number): void {
      patchState(store, { isLoading: true, error: null });
      http.post<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.items, { skuId, quantity }).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of({ items: [], version: undefined });
        }),
      ).subscribe((res) => {
        patchState(store, {
          items: res.items.map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    },

    updateQuantity(skuId: number, quantity: number): void {
      const ver = store.version();
      const body: Record<string, unknown> = { quantity };
      if (ver !== null) body['version'] = ver;
      patchState(store, { isLoading: true, error: null });
      http.put<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.item(skuId), body).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of({ items: [], version: undefined });
        }),
      ).subscribe((res) => {
        patchState(store, {
          items: res.items.map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    },

    removeItem(skuId: number): void {
      patchState(store, { isLoading: true, error: null });
      http.delete<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.item(skuId)).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of({ items: [], version: undefined });
        }),
      ).subscribe((res) => {
        patchState(store, {
          items: res.items.map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    },

    clearCart(): void {
      http.delete(ApiEndpoints.cart.base).pipe(
        catchError(() => of(null)),
      ).subscribe(() => patchState(store, { items: [], version: null, error: null }));
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
