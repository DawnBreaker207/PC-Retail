import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CheckoutRequest, CheckoutResponse, VnpayInitResponse } from '@core/models/common.model';
import { map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private http = inject(HttpClient);

  checkout(data: CheckoutRequest) {
    const headers = new HttpHeaders().set('Idempotency-Key', data.idempotencyKey);
    return this.http.post<any>(ApiEndpoints.orders.checkout, data, { headers }).pipe(
      map(res => (res?.data ?? res) as CheckoutResponse)
    );
  }

  guestCheckout(data: {
    idempotencyKey: string;
    fulfillmentType: string;
    shippingAddress: unknown;
    guestToken: string;
    guestPhone?: string;
  }) {
    return this.http.post<any>(ApiEndpoints.orders.guestCheckout, data).pipe(
      map(res => (res?.data ?? res) as CheckoutResponse)
    );
  }

  initiatePayment(orderId: number) {
    return this.http.post<any>(ApiEndpoints.payment.create, { orderId }).pipe(
      map(res => (res?.data ?? res) as VnpayInitResponse)
    );
  }

  otpChallenge() {
    return this.http.get<any>(ApiEndpoints.auth.otpChallenge).pipe(
      map(res => (res?.data ?? res) as { challengeId: string; question: string })
    );
  }

  requestPhoneOtp(phone: string, challengeId: string, challengeAnswer: string) {
    return this.http.post<any>(
      ApiEndpoints.auth.phoneOtpRequest,
      { phone, challengeId, challengeAnswer },
    ).pipe(
      map(res => (res?.data ?? res) as { sent: boolean; debugCode?: string })
    );
  }

  verifyPhoneOtp(phone: string, code: string) {
    return this.http.post<any>(ApiEndpoints.auth.phoneOtpVerify, {
      phone,
      code,
    }).pipe(
      map(res => (res?.data ?? res) as { verified: boolean })
    );
  }
}
