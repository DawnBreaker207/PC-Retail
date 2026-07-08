import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { BuilderSlot, BuilderSelection, BuilderValidation, ResponsePage, Sku, BuildTemplate, BuildTemplateDetail } from '@core/models/common.model';

export interface BuilderSlotsResponse {
  content: BuilderSlot[];
}

@Injectable({ providedIn: 'root' })
export class BuilderService {
  private http = inject(HttpClient);

  getSlots(): Observable<BuilderSlotsResponse> {
    return this.http.get<BuilderSlotsResponse>(ApiEndpoints.builder.slots);
  }

  validate(selections: BuilderSelection[]): Observable<BuilderValidation> {
    const payload = selections.map(s => ({ slotType: s.slotType, skuId: s.sku.id }));
    return this.http.post<BuilderValidation>(ApiEndpoints.builder.validate, { selections: payload });
  }

  getTemplates(page = 0, size = 12): Observable<ResponsePage<BuildTemplate>> {
    return this.http.get<ResponsePage<BuildTemplate>>(`${ApiEndpoints.builder.templates}?page=${page}&size=${size}`);
  }

  getTemplateDetail(id: number): Observable<BuildTemplateDetail> {
    return this.http.get<BuildTemplateDetail>(ApiEndpoints.builder.templateDetail(id));
  }
}
