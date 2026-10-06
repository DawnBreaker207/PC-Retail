import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CreateReviewRequest, Order, ResponsePage, Review } from '@core/models/common.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);

  getMyOrders(params: { page?: number; size?: number; status?: string } = {}) {
    let httpParams = new HttpParams();
    if (params.page) httpParams = httpParams.set('page', params.page);
    if (params.size) httpParams = httpParams.set('size', params.size);
    if (params.status) httpParams = httpParams.set('status', params.status);
    return this.http.get<ResponsePage<Order>>(ApiEndpoints.orders.base, {
      params: httpParams,
    });
  }

  getOrderDetail(id: number) {
    return this.http.get<Order>(ApiEndpoints.orders.detail(id));
  }

  cancelOrder(id: number) {
    return this.http.post(ApiEndpoints.orders.cancelSubOrder(id), {});
  }

  createReview(data: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(ApiEndpoints.reviews.create, data);
  }

  requestReturn(orderId: number, subOrderId: number, reason: string) {
    return this.http.post(ApiEndpoints.returns.request(orderId), { subOrderId, reason, images: [] });
  }

  getReturnBySubOrder(subOrderId: number) {
    return this.http.get(ApiEndpoints.returns.bySubOrder(subOrderId));
  }

  disputeReturn(requestId: number, note: string) {
    return this.http.post(ApiEndpoints.returns.dispute(requestId), { note });
  }
}
