import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CreateReviewRequest, ResponsePage, Review, SkuRatingSummary } from '@core/models/common.model';

export interface RatingDto {
  id: number;
  userId: number;
  skuId: number;
  score: number;
  comment: string;
  imageUrls?: string[];
  status?: string;
  createdAt: string;
}

export interface BackendResponse<T> {
  code?: number;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class RatingService {
  private http = inject(HttpClient);

  getSkuRatings(skuId: number, page = 0, size = 10): Observable<RatingDto[]> {
    return this.http.get<BackendResponse<ResponsePage<RatingDto>> | ResponsePage<RatingDto>>(
      ApiEndpoints.ratings.bySku(skuId, page, size)
    ).pipe(
      map((res: any) => {
        if (res && res.data && res.data.content) {
          return res.data.content;
        }
        if (res && res.content) {
          return res.content;
        }
        return [];
      }),
      catchError(() => of([]))
    );
  }

  getSkuSummary(skuId: number): Observable<SkuRatingSummary | null> {
    return this.http.get<BackendResponse<SkuRatingSummary> | SkuRatingSummary>(
      ApiEndpoints.ratings.summary(skuId)
    ).pipe(
      map((res: any) => {
        if (res && res.data) {
          return res.data;
        }
        return res || null;
      }),
      catchError(() => of(null))
    );
  }

  submitRating(req: CreateReviewRequest): Observable<RatingDto> {
    const payload = {
      skuId: req.skuId ?? req.orderItemId,
      orderId: req.orderId,
      score: req.score ?? req.rating ?? 5,
      comment: req.comment,
      imageUrls: req.imageUrls ?? req.images ?? [],
    };
    return this.http.post<BackendResponse<RatingDto> | RatingDto>(
      ApiEndpoints.ratings.submit,
      payload
    ).pipe(
      map((res: any) => (res && res.data ? res.data : res))
    );
  }
}
