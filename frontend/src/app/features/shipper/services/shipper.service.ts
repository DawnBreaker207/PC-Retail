import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { ResponsePage, SubOrder } from '@core/models/common.model';

@Injectable({ providedIn: 'root' })
export class ShipperService {
  private http = inject(HttpClient);

  getMyOrders(page = 0, size = 20, status?: string): Observable<ResponsePage<SubOrder>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ResponsePage<SubOrder>>(ApiEndpoints.shipper.myOrders, { params });
  }

  pickup(subOrderId: number): Observable<SubOrder> {
    return this.http.patch<SubOrder>(ApiEndpoints.shipper.pickup(subOrderId), {});
  }

  delivered(subOrderId: number, codCollected = false): Observable<SubOrder> {
    return this.http.patch<SubOrder>(
      ApiEndpoints.shipper.delivered(subOrderId, codCollected),
      {},
    );
  }

  failed(subOrderId: number, note: string): Observable<SubOrder> {
    let params = new HttpParams().set('note', note);
    return this.http.patch<SubOrder>(ApiEndpoints.shipper.failed(subOrderId), {}, { params });
  }
}
