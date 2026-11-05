import { Component, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  Laptop,
  Monitor,
  Cpu,
  HardDrive,
  Database,
  Headphones,
  Tv,
  Keyboard,
  Mouse,
  Layers,
  ChevronRight,
  LucideIconData,
} from 'lucide-angular';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-grid',
  imports: [RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './category-grid.component.html',
})
export class CategoryGridComponent {
  readonly ChevronRight = ChevronRight;
  private mock = inject(MockDataService);

  private iconMap: Record<string, LucideIconData> = {
    laptop: Laptop,
    'laptop-gaming': Laptop,
    pc: Monitor,
    'linh-kien': Cpu,
    'case-nguon-tan': HardDrive,
    'o-cung-ram': Database,
    'loa-micro-webcam': Headphones,
    'man-hinh': Tv,
    'ban-phim': Keyboard,
    chuot: Mouse,
    'tai-nghe': Headphones,
  };

  categories = computed(() => {
    const products = this.mock.generateProducts();
    return this.mock.categories.map((cat) => ({
      ...cat,
      iconComponent: this.iconMap[cat.slug] || Layers,
      productCount: products.filter((p) => p.categoryName === cat.slug).length,
    }));
  });
}
