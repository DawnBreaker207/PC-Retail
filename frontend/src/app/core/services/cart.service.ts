import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CartItem } from '@core/models/common.model';
import { tap, Observable } from 'rxjs';

interface CartResponse {
  items: CartItem[];
  version?: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private version = signal<number | null>(null);

  private itemsSignal = signal<CartItem[]>([]);
  readonly items = this.itemsSignal.asReadonly();
  readonly isLoading = signal(false);

  readonly totalCount = computed(() => this.items().length);
  readonly totalAmount = computed(() =>
    this.items().reduce((sum, i) => sum + i.price * i.quantity, 0),
  );
  readonly selectedItems = computed(() => this.items().filter((i) => i.selected));
  readonly selectedAmount = computed(() =>
    this.selectedItems().reduce((sum, i) => sum + i.price * i.quantity, 0),
  );

  loadCart() {
    this.isLoading.set(true);
    this.http
      .get<CartResponse>(ApiEndpoints.cart.base)
      .subscribe((res) => {
        this.itemsSignal.set(res.items.map((i) => ({ ...i, selected: true })));
        if (res.version) this.version.set(res.version);
        this.isLoading.set(false);
      });
  }

  addItem(skuId: number, quantity: number): Observable<unknown> {
    return this.http.post(ApiEndpoints.cart.items, { skuId, quantity }).pipe(
      tap(() => this.loadCart()),
    );
  }

  updateQuantity(skuId: number, quantity: number): Observable<CartResponse> {
    const ver = this.version();
    const body: Record<string, unknown> = { quantity };
    if (ver !== null) body['version'] = ver;
    return this.http
      .put<CartResponse>(ApiEndpoints.cart.item(skuId), body)
      .pipe(tap((res) => { if (res?.version) this.version.set(res.version); }));
  }

  removeItem(skuId: number): Observable<unknown> {
    return this.http.delete(ApiEndpoints.cart.item(skuId)).pipe(tap(() => this.loadCart()));
  }

  clearCart(): Observable<unknown> {
    return this.http.delete(ApiEndpoints.cart.base).pipe(tap(() => this.itemsSignal.set([])));
  }

  toggleSelect(skuId: number) {
    this.itemsSignal.update((items) =>
      items.map((i) => (i.skuId === skuId ? { ...i, selected: !i.selected } : i)),
    );
  }

  toggleSelectAll() {
    const allSelected = this.items().every((i) => i.selected);
    this.itemsSignal.update((items) =>
      items.map((i) => ({ ...i, selected: !allSelected })),
    );
  }
}
