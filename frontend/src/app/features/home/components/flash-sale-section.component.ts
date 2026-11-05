import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Zap } from 'lucide-angular';
import { MockDataService } from '@core/services/mock-data.service';
import { PromotionService } from '@core/services/promotion.service';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';

@Component({
  selector: 'app-flash-sale-section',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, CurrencyFormatPipe, FallbackImageDirective],
  templateUrl: './flash-sale-section.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './flash-sale-section.component.css',
})
export class FlashSaleSectionComponent {
  private mock = inject(MockDataService);
  private promotionService = inject(PromotionService);
  readonly ZapIcon = Zap;

  campaign = this.mock.getFlashSale();
  campaignTitle = signal(this.campaign.campaignName);
  countdown = '03:15:42';
  flashCategories = ['Tất cả', 'Laptop', 'Linh kiện', 'Phụ kiện', 'Màn hình'];
  activeTab = 0;

  constructor() {
    this.promotionService.getActiveFlashSales().subscribe((activeList) => {
      if (activeList && activeList.length > 0) {
        this.campaignTitle.set(activeList[0].name || this.campaign.campaignName);
      }
    });
  }

  get filteredItems() {
    const items = this.campaign.items;
    if (this.activeTab === 0) return items;
    const catMap: Record<number, string> = {
      1: 'Laptop',
      2: 'Linh kiện',
      3: 'Phụ kiện',
      4: 'Màn hình',
    };
    const cat = catMap[this.activeTab];
    return items.filter((i) => i.category === cat);
  }

  soldPercent(item: { remaining: number; total: number }): number {
    return Math.round(((item.total - item.remaining) / item.total) * 100);
  }
}
