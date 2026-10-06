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

  guestCheckout(data: {
    idempotencyKey: string;
    fulfillmentType: string;
    shippingAddress: unknown;
    guestToken: string;
    guestPhone?: string;
  }) {
    return this.http.post<CheckoutResponse>(ApiEndpoints.orders.guestCheckout, data);
  }

  initiatePayment(orderId: number) {
    return this.http.post<VnpayInitResponse>(ApiEndpoints.payment.create, { orderId });
  }

  otpChallenge() {
    return this.http.get<{ challengeId: string; question: string }>(
      ApiEndpoints.auth.otpChallenge,
    );
  }

  requestPhoneOtp(phone: string, challengeId: string, challengeAnswer: string) {
    return this.http.post<{ sent: boolean; debugCode?: string }>(
      ApiEndpoints.auth.phoneOtpRequest,
      { phone, challengeId, challengeAnswer },
    );
  }

  verifyPhoneOtp(phone: string, code: string) {
    return this.http.post<{ verified: boolean }>(ApiEndpoints.auth.phoneOtpVerify, {
      phone,
      code,
    });
  }
}
