import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { MockDataService } from '@core/services/mock-data.service';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';

@Component({
  selector: 'app-flash-sale-section',
  imports: [RouterLink, NzProgressModule, CurrencyFormatPipe, FallbackImageDirective],
  templateUrl: './flash-sale-section.component.html',
  styleUrl: './flash-sale-section.component.css',
})
export class FlashSaleSectionComponent {
  private mock = inject(MockDataService);

  campaign = this.mock.getFlashSale();
  countdown = '03:15:42';
  flashCategories = ['Tất cả', 'Laptop', 'Linh kiện', 'Phụ kiện', 'Màn hình'];
  activeTab = 0;

  get filteredItems() {
    const items = this.campaign.items;
    if (this.activeTab === 0) return items;
    const catMap: Record<number, string> = { 1: 'Laptop', 2: 'Linh kiện', 3: 'Phụ kiện', 4: 'Màn hình' };
    const cat = catMap[this.activeTab];
    return items.filter(i => i.category === cat);
  }

  soldPercent(item: { remaining: number; total: number }): number {
    return Math.round(((item.total - item.remaining) / item.total) * 100);
  }
}
