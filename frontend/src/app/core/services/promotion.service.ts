import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Voucher, VoucherValidationResult } from '@core/models/common.model';

export interface FlashSaleCampaignBackend {
  id: number;
  name: string;
  startTime: string;
  endTime: string;
  status: 'UPCOMING' | 'ACTIVE' | 'ENDED';
}

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private http = inject(HttpClient);

  getActiveFlashSales(): Observable<FlashSaleCampaignBackend[]> {
    return this.http.get<{ data?: FlashSaleCampaignBackend[] } | FlashSaleCampaignBackend[]>(
      ApiEndpoints.promotion.flashSalesActive
    ).pipe(
      map((res: any) => {
        if (res && res.data && Array.isArray(res.data)) {
          return res.data;
        }
        if (Array.isArray(res)) {
          return res;
        }
        return [];
      }),
      catchError(() => of([]))
    );
  }

  validateVoucher(code: string, orderTotal: number): Observable<VoucherValidationResult> {
    return this.http.post<{ data?: VoucherValidationResult } | VoucherValidationResult>(
      ApiEndpoints.promotion.validateVoucher,
      { code, orderTotal }
    ).pipe(
      map((res: any) => {
        if (res && res.data) {
          return res.data;
        }
        return res;
      }),
      catchError((err) => {
        const message = err.error?.message || 'Mã giảm giá không hợp lệ hoặc đã hết hạn';
        return of({ valid: false, discountAmount: 0, errorMessage: message });
      })
    );
  }

  getAvailableVouchers(): Observable<Voucher[]> {
    return this.http.get<{ data?: { content?: Voucher[] } } | { content?: Voucher[] }>(
      ApiEndpoints.promotion.vouchers
    ).pipe(
      map((res: any) => {
        if (res?.data?.content) return res.data.content;
        if (res?.content) return res.content;
        if (Array.isArray(res)) return res;
        return [];
      }),
      catchError(() => of([]))
    );
  }
}
