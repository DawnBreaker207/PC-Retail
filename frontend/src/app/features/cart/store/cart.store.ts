import { signalStore, withState, withComputed, withMethods, withHooks, patchState } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CartItem } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AuthService } from '@core/auth/auth.service';
import { getGuestToken } from '@core/utils/guest-token';
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
  withMethods((store, http = inject(HttpClient), auth = inject(AuthService)) => {
    const isGuest = () => !auth.isAuthenticated();
    const guestParams = () => new HttpParams().set('token', getGuestToken());
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const toItems = (res: { items: any[]; version?: number }) => {
      patchState(store, {
        // API thật trả {skuId, skuName, primaryImageUrl, unitPrice}; mock trả {name, imageUrl, price} — chuẩn hoá 1 chỗ
        items: (res.items ?? []).map((i) => ({
          skuId: i['skuId'],
          name: (i['name'] ?? i['skuName'] ?? '') as string,
          slug: (i['slug'] ?? '') as string,
          imageUrl: (i['imageUrl'] ?? i['primaryImageUrl'] ?? '') as string,
          price: Number(i['price'] ?? i['unitPrice'] ?? 0),
          quantity: i['quantity'],
          stock: i['stock'],
          brandName: (i['brandName'] ?? '') as string,
          selected: true,
        })) as CartItem[],
        version: res.version ?? null,
        isLoading: false,
      });
    };
    const fail = (err: unknown) => {
      const message = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
      patchState(store, { isLoading: false, error: message });
      return of({ items: [], version: undefined });
    };
    const loadGuest = () => {
      patchState(store, { isLoading: true, error: null });
      http.get<{ items: CartItem[] }>(ApiEndpoints.cart.guestBase, { params: guestParams() }).pipe(
        catchError(fail),
      ).subscribe((res) => toItems({ items: res.items ?? [], version: undefined }));
    };
    // POST returns a single item, not the cart — always reload after writing.
    const loadMember = () => {
      patchState(store, { isLoading: true, error: null });
      http.get<{ items: CartItem[]; version?: number }>(ApiEndpoints.cart.base).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of({ items: [], version: undefined });
        }),
      ).subscribe((res) => {
        patchState(store, {
          items: (res.items ?? []).map((i) => ({ ...i, selected: true })),
          version: res.version ?? null,
          isLoading: false,
        });
      });
    };

    return {
    loadCart(): void {
      if (isGuest()) {
        loadGuest();
        return;
      }
      loadMember();
    },

    addItem(skuId: number, quantity: number): void {
      patchState(store, { isLoading: true, error: null });
      if (isGuest()) {
        http.post(ApiEndpoints.cart.guestItems, { skuId, quantity }, { params: guestParams() }).pipe(
          catchError(fail),
        ).subscribe(() => loadGuest());
        return;
      }
      http.post(ApiEndpoints.cart.items, { skuId, quantity }).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of(null);
        }),
      ).subscribe((res) => {
        if (res) loadMember();
      });
    },

    updateQuantity(skuId: number, quantity: number): void {
      if (isGuest()) {
        // Guest BE chưa có update: remove + add lại
        patchState(store, { isLoading: true, error: null });
        http.delete(ApiEndpoints.cart.guestItem(skuId), { params: guestParams() }).pipe(
          catchError(fail),
        ).subscribe(() => {
          http.post(ApiEndpoints.cart.guestItems, { skuId, quantity }, { params: guestParams() }).pipe(
            catchError(fail),
          ).subscribe(() => loadGuest());
        });
        return;
      }
      patchState(store, { isLoading: true, error: null });
      http.put(ApiEndpoints.cart.items, { skuId, quantity }).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of(null);
        }),
      ).subscribe((res) => {
        if (res) loadMember();
      });
    },

    removeItem(skuId: number): void {
      if (isGuest()) {
        patchState(store, { isLoading: true, error: null });
        http.delete(ApiEndpoints.cart.guestItem(skuId), { params: guestParams() }).pipe(
          catchError(fail),
        ).subscribe(() => loadGuest());
        return;
      }
      patchState(store, { isLoading: true, error: null });
      http.delete(ApiEndpoints.cart.item(skuId)).pipe(
        catchError((err) => {
          const msg = (err as { error?: { message?: string } })?.error?.message || (err as Error)?.message || 'Có lỗi xảy ra';
          patchState(store, { isLoading: false, error: msg });
          return of(null);
        }),
      ).subscribe((res) => {
        if (res !== undefined) loadMember();
      });
    },

    clearCart(): void {
      if (isGuest()) {
        http.delete(ApiEndpoints.cart.guestBase, { params: guestParams() }).pipe(
          catchError(() => of(null)),
        ).subscribe(() => patchState(store, { items: [], version: null, error: null }));
        return;
      }      http.delete(ApiEndpoints.cart.base).pipe(
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
    };
  }),
  withHooks({
    onInit(store) {
      store.loadCart();
    },
  }),
);
