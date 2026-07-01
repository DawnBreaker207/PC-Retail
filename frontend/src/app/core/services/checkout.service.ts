import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CheckoutRequest, CheckoutResponse, VnpayInitResponse } from '@core/models/common.model';

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private http = inject(HttpClient);

  checkout(data: CheckoutRequest) {
    const headers = new HttpHeaders().set('Idempotency-Key', data.idempotencyKey);
    return this.http.post<CheckoutResponse>(ApiEndpoints.orders.checkout, data, { headers });
  }

  initiatePayment(orderId: number) {
    return this.http.post<VnpayInitResponse>(ApiEndpoints.payment.vnpayInit, { orderId });
  }
}
