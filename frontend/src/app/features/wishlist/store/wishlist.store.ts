import { signalStore, withState, withComputed, withMethods, withHooks, patchState } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AuthService } from '@core/auth/auth.service';

const STORAGE_KEY = 'pc-retail-wishlist';

interface WishlistState {
  skuIds: number[];
}

function readStorage(): number[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as number[]) : [];
  } catch {
    return [];
  }
}

function writeStorage(skuIds: number[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(skuIds));
  } catch {
    // ignore storage errors (private mode, quota, etc.)
  }
}

export const WishlistStore = signalStore(
  { providedIn: 'root' },
  withState<WishlistState>({ skuIds: [] }),
  withComputed(({ skuIds }) => ({
    count: computed(() => skuIds().length),
  })),
  withMethods((store, http = inject(HttpClient), auth = inject(AuthService)) => ({
    has(skuId: number): boolean {
      return store.skuIds().includes(skuId);
    },
    // Đồng bộ BE khi đã login, rớt về localStorage khi guest/lỗi
    syncFromServer(): void {
      if (!auth.isAuthenticated()) return;
      http.get<{ skuId: number }[]>(ApiEndpoints.wishlist.base).pipe(
        catchError(() => of([])),
      ).subscribe((list) => {
        const arr = Array.isArray(list) ? list : [];
        const ids = (arr as unknown[]).map((i) =>
          typeof i === 'number' ? i : (i as { skuId: number }).skuId,
        ).filter((v): v is number => typeof v === 'number');
        if (ids.length) {
          patchState(store, { skuIds: ids });
          writeStorage(ids);
        }
      });
    },
    toggle(skuId: number): void {
      const current = store.skuIds();
      const adding = !current.includes(skuId);
      const next = adding ? [...current, skuId] : current.filter((id) => id !== skuId);
      patchState(store, { skuIds: next });
      writeStorage(next);
      if (!auth.isAuthenticated()) return;
      const req = adding
        ? http.post(ApiEndpoints.wishlist.base, { skuId })
        : http.delete(ApiEndpoints.wishlist.item(skuId));
      req.pipe(catchError(() => of(null))).subscribe();
    },
    remove(skuId: number): void {
      const next = store.skuIds().filter((id) => id !== skuId);
      patchState(store, { skuIds: next });
      writeStorage(next);
      if (!auth.isAuthenticated()) return;
      http.delete(ApiEndpoints.wishlist.item(skuId)).pipe(catchError(() => of(null))).subscribe();
    },
  })),
  withHooks({
    onInit(store) {
      patchState(store, { skuIds: readStorage() });
    },
  }),
);
