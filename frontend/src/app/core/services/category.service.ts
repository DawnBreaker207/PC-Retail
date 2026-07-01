import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { shareReplay } from 'rxjs';

export interface Category {
  id: number;
  name: string;
  slug: string;
  parentId?: number;
  imageUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);

  categories$ = this.http.get<Category[]>(ApiEndpoints.catalog.categories).pipe(
    shareReplay(1),
  );
}
