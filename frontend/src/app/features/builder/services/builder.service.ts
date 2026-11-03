import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { BuilderSlot, BuilderSelection, BuilderValidation, ResponsePage, Sku, BuildTemplate, BuildTemplateDetail } from '@core/models/common.model';

export interface BuilderSlotsResponse {
  content: BuilderSlot[];
}

export const DEFAULT_BUILDER_SLOTS: BuilderSlot[] = [
  { slotType: 'CPU', slotName: 'Bộ vi xử lý (CPU)', isRequired: true, isRemovable: true },
  { slotType: 'Mainboard', slotName: 'Bo mạch chủ (Mainboard)', isRequired: true, isRemovable: true },
  { slotType: 'RAM', slotName: 'Bộ nhớ trong (RAM)', isRequired: true, isRemovable: true },
  { slotType: 'GPU', slotName: 'Card màn hình (VGA)', isRequired: true, isRemovable: true },
  { slotType: 'Storage', slotName: 'Ổ cứng lưu trữ (SSD)', isRequired: true, isRemovable: true },
  { slotType: 'PSU', slotName: 'Nguồn máy tính (PSU)', isRequired: true, isRemovable: true },
  { slotType: 'Case', slotName: 'Vỏ máy tính (Case)', isRequired: true, isRemovable: true },
  { slotType: 'Cooler', slotName: 'Tản nhiệt CPU', isRequired: false, isRemovable: true },
];

@Injectable({ providedIn: 'root' })
export class BuilderService {
  private http = inject(HttpClient);

  getSlots(): Observable<BuilderSlotsResponse> {
    return this.http.get<BuilderSlotsResponse>(ApiEndpoints.builder.slots).pipe(
      catchError(() => of({ content: DEFAULT_BUILDER_SLOTS }))
    );
  }

  validate(selections: BuilderSelection[]): Observable<BuilderValidation> {
    const payload = selections.map(s => ({ slotType: s.slotType, skuId: s.sku.id }));
    return this.http.post<BuilderValidation>(ApiEndpoints.builder.validate, { selections: payload }).pipe(
      catchError(() => of({ valid: true, conflicts: [], warnings: [] }))
    );
  }

  getTemplates(page = 0, size = 12): Observable<ResponsePage<BuildTemplate>> {
    return this.http.get<ResponsePage<BuildTemplate>>(`${ApiEndpoints.builder.templates}?page=${page}&size=${size}`).pipe(
      catchError(() => of({ content: [], pagination: { pageNumber: page, pageSize: size, totalElements: 0 } }))
    );
  }

  getTemplateDetail(id: number): Observable<BuildTemplateDetail> {
    return this.http.get<BuildTemplateDetail>(ApiEndpoints.builder.templateDetail(id));
  }
}
