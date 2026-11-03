import { Component, inject, signal, computed } from '@angular/core';
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
  Flame,
  ArrowRight,
  Sparkles,
  LucideIconData,
} from 'lucide-angular';
import { MockDataService } from '@core/services/mock-data.service';
import { CATEGORY_MEGA_MENUS, CategoryMegaMenuData } from '@core/mocks/mega-menu.mock';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';

@Component({
  selector: 'app-category-sidebar',
  imports: [RouterLink, LucideAngularModule, FallbackImageDirective],
  templateUrl: './category-sidebar.component.html',
  styleUrl: './category-sidebar.component.css',
})
export class CategorySidebarComponent {
  readonly ChevronRight = ChevronRight;
  readonly FlameIcon = Flame;
  readonly ArrowRightIcon = ArrowRight;
  readonly SparklesIcon = Sparkles;

  private mock = inject(MockDataService);

  hoveredCategory = signal<string | null>(null);

  activeMegaMenu = computed<CategoryMegaMenuData | null>(() => {
    const slug = this.hoveredCategory();
    if (!slug) return null;
    return CATEGORY_MEGA_MENUS[slug] || null;
  });

  private iconMap: Record<string, LucideIconData> = {
    'laptop': Laptop,
    'laptop-gaming': Laptop,
    'pc': Monitor,
    'linh-kien': Cpu,
    'case-nguon-tan': HardDrive,
    'o-cung-ram': Database,
    'loa-micro-webcam': Headphones,
    'man-hinh': Tv,
    'ban-phim': Keyboard,
    'chuot': Mouse,
    'tai-nghe': Headphones,
  };

  categories = this.mock.categories.map((cat) => ({
    ...cat,
    iconComponent: this.iconMap[cat.slug] || Layers,
  }));

  onItemHover(slug: string) {
    this.hoveredCategory.set(slug);
  }

  onMouseLeave() {
    this.hoveredCategory.set(null);
  }
}
