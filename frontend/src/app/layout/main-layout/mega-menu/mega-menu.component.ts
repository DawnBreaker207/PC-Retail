import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { CategorySidebarComponent } from '@shared/components/category-sidebar.component';
import { CategoryMenuService } from '@core/services/category-menu.service';

interface MenuGroup {
  label: string;
  slug: string;
  icon: string;
  children?: { title: string; items: { label: string; queryParams: Record<string, string> }[] }[];
}

function priceItem(category: string, label: string, minPrice?: number, maxPrice?: number) {
  const queryParams: Record<string, string> = { category };
  if (minPrice != null) queryParams['minPrice'] = String(minPrice);
  if (maxPrice != null) queryParams['maxPrice'] = String(maxPrice);
  return { label, queryParams };
}

function brandItem(category: string, brand: string) {
  return { label: brand, queryParams: { category, brand } };
}

const DEFAULT_GROUPS: MenuGroup[] = [
  {
    label: 'Laptop',
    slug: 'laptop',
    icon: 'laptop',
    children: [
      {
        title: 'Thương hiệu',
        items: ['ASUS', 'ACER', 'MSI', 'LENOVO', 'DELL', 'LG'].map((b) => brandItem('laptop', b)),
      },
      {
        title: 'Giá bán',
        items: [
          priceItem('laptop', 'Dưới 15 triệu', undefined, 15_000_000),
          priceItem('laptop', '15 - 25 triệu', 15_000_000, 25_000_000),
          priceItem('laptop', '25 - 40 triệu', 25_000_000, 40_000_000),
          priceItem('laptop', 'Trên 40 triệu', 40_000_000, undefined),
        ],
      },
    ],
  },
  {
    label: 'Laptop Gaming',
    slug: 'laptop-gaming',
    icon: 'play-circle',
    children: [
      {
        title: 'Thương hiệu',
        items: ['ASUS', 'MSI', 'ACER', 'LENOVO', 'GIGABYTE'].map((b) => brandItem('laptop-gaming', b)),
      },
      {
        title: 'Giá bán',
        items: [
          priceItem('laptop-gaming', 'Dưới 20tr', undefined, 20_000_000),
          priceItem('laptop-gaming', '20 - 30tr', 20_000_000, 30_000_000),
          priceItem('laptop-gaming', '30 - 50tr', 30_000_000, 50_000_000),
          priceItem('laptop-gaming', 'Trên 50tr', 50_000_000, undefined),
        ],
      },
    ],
  },
  {
    label: 'PC GVN',
    slug: 'pc',
    icon: 'desktop',
    children: [
      {
        title: 'Thương hiệu',
        items: ['INTEL', 'AMD'].map((b) => brandItem('pc', b)),
      },
      {
        title: 'Giá bán',
        items: [
          priceItem('pc', 'Dưới 20 triệu', undefined, 20_000_000),
          priceItem('pc', '20 - 35 triệu', 20_000_000, 35_000_000),
          priceItem('pc', 'Trên 35 triệu', 35_000_000, undefined),
        ],
      },
    ],
  },
  {
    label: 'Main, CPU, VGA',
    slug: 'linh-kien',
    icon: 'tool',
  },
  {
    label: 'Màn hình',
    slug: 'man-hinh',
    icon: 'monitor',
  },
  {
    label: 'Phụ kiện',
    slug: 'phu-kien',
    icon: 'appstore',
  },
];

@Component({
  selector: 'app-mega-menu',
  imports: [RouterLink, NzMenuModule, NzIconModule, CategorySidebarComponent],
  templateUrl: './mega-menu.component.html',
  styleUrl: './mega-menu.component.css',
})
export class MegaMenuComponent {
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  menu = inject(CategoryMenuService);
  categories = signal<MenuGroup[]>(DEFAULT_GROUPS);
  isHomeRoute = signal(this.router.url === '/');

  private hoverOpened = false;

  constructor() {
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.menu.close();
      this.hoverOpened = false;
      this.isHomeRoute.set(this.router.url === '/');
    });
  }

  onCategoryClick(): void {
    this.hoverOpened = false;
    this.menu.toggle();
  }

  onCategoryHoverEnter(): void {
    if (this.isHomeRoute() && this.menu.isSidebarInViewport() && !this.menu.isOpen()) {
      this.hoverOpened = true;
      this.menu.isOpen.set(true);
    }
  }

  onCategoryHoverLeave(): void {
    if (this.hoverOpened) {
      this.hoverOpened = false;
      this.menu.close();
    }
  }
}
