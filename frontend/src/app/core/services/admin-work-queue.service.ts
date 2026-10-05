import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable, catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AdminReturnRequest, Order } from '@core/models/common.model';

function asArray<T>(res: T[] | { content: T[] } | null): T[] {
  if (Array.isArray(res)) return res;
  if (res && Array.isArray((res as { content: T[] }).content)) return (res as { content: T[] }).content;
  return [];
}

@Injectable({ providedIn: 'root' })
export class AdminWorkQueueService {
  private http = inject(HttpClient);

  getPendingReturns(): Observable<AdminReturnRequest[]> {
    const params = new HttpParams().set('status', 'REQUESTED');
    return this.http.get<AdminReturnRequest[]>(ApiEndpoints.admin.returns, { params }).pipe(
      catchError(() => of([] as AdminReturnRequest[])),
      map((res) => asArray(res)),
    );
  }

  getPendingReturnsCount(): Observable<number> {
    return this.getPendingReturns().pipe(map((items) => items.length));
  }

  getPendingPaymentOrders(): Observable<Order[]> {
    return this.http.get<Order[] | { content: Order[] }>(ApiEndpoints.admin.orders).pipe(
      catchError(() => of([] as Order[])),
      map((res) => asArray(res).filter((o) => o.subOrders.some((s) => s.status === 'PENDING_PAYMENT'))),
    );
  }

  getPendingPaymentOrdersCount(): Observable<number> {
    return this.getPendingPaymentOrders().pipe(map((items) => items.length));
  }
}
