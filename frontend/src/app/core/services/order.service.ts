import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CreateReviewRequest, Order, ResponsePage, Review, SubmitReturnPayload } from '@core/models/common.model';

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
    const payload = {
      skuId: data.skuId ?? data.orderItemId,
      orderId: data.orderId,
      score: data.score ?? data.rating ?? 5,
      comment: data.comment,
      imageUrls: data.imageUrls ?? data.images ?? [],
    };
    return this.http.post<Review>(ApiEndpoints.reviews.create, payload);
  }

  requestReturn(subOrderId: number, payload: SubmitReturnPayload) {
    return this.http.post(ApiEndpoints.returns.submit(subOrderId), payload);
  }

  getReturnBySubOrder(subOrderId: number) {
    return this.http.get(ApiEndpoints.returns.bySubOrder(subOrderId));
  }

  disputeReturn(requestId: number, note: string) {
    return this.http.post(ApiEndpoints.returns.dispute(requestId), { note });
  }

  requestOrderInvoice(orderId: number) {
    return this.http.post(`${ApiEndpoints.orders.base}/${orderId}/invoice`, {});
  }

  getOrderInvoice(orderId: number) {
    return this.http.get(`${ApiEndpoints.orders.base}/${orderId}/invoice`);
  }
}
