import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { FlashSaleSectionComponent } from '@features/home/components/flash-sale-section.component';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-promotions',
  imports: [RouterLink, NzBreadCrumbModule, FlashSaleSectionComponent, FallbackImageDirective],
  templateUrl: './promotions.component.html',
})
export class PromotionsComponent {
  private mock = inject(MockDataService);

  promoBanners = this.mock.getPromoBanners();
  promoCampaigns = this.mock.getPromoCampaigns();

  // API thật, rớt về mock khi BE chưa có campaign
  activeFlashSales = signal<unknown[]>([]);

  constructor() {
    inject(HttpClient).get<unknown[]>(ApiEndpoints.promotion.flashSalesActive).pipe(
      catchError(() => of([])),
    ).subscribe((list) => this.activeFlashSales.set(Array.isArray(list) ? list : []));
  }
}
