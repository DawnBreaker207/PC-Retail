import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import {
  LucideAngularModule,
  LayoutGrid,
  ChevronDown,
  Wrench,
  Laptop,
  Monitor,
  Cpu,
  Tv,
  LucideIconData,
} from 'lucide-angular';
import { CategorySidebarComponent } from '@shared/components/category-sidebar.component';
import { CategoryMenuService } from '@core/services/category-menu.service';

interface MenuGroup {
  label: string;
  slug: string;
  icon: LucideIconData;
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
    icon: Laptop,
    children: [
      {
        title: 'Thương hiệu',
        items: ['ASUS', 'ACER', 'MSI', 'LENOVO', 'DELL', 'HP', 'LG Gram'].map((b) => brandItem('laptop', b)),
      },
      {
        title: 'Mức giá',
        items: [
          priceItem('laptop', 'Dưới 15 triệu', undefined, 15_000_000),
          priceItem('laptop', '15 - 20 triệu', 15_000_000, 20_000_000),
          priceItem('laptop', '20 - 25 triệu', 20_000_000, 25_000_000),
          priceItem('laptop', '25 - 30 triệu', 25_000_000, 30_000_000),
          priceItem('laptop', 'Trên 30 triệu', 30_000_000, undefined),
        ],
      },
      {
        title: 'Nhu cầu sử dụng',
        items: [
          { label: 'Học tập - Văn phòng', queryParams: { category: 'laptop', q: 'van-phong' } },
          { label: 'Đồ họa - Thiết kế', queryParams: { category: 'laptop', q: 'do-hoa' } },
          { label: 'Mỏng nhẹ cao cấp', queryParams: { category: 'laptop', q: 'mong-nhe' } },
          { label: 'Doanh nhân VIP', queryParams: { category: 'laptop', q: 'doanh-nhan' } },
        ],
      },
      {
        title: 'Cấu hình nổi bật',
        items: [
          { label: 'Intel Core Ultra', queryParams: { category: 'laptop', q: 'intel-ultra' } },
          { label: 'AMD Ryzen AI', queryParams: { category: 'laptop', q: 'ryzen-ai' } },
          { label: 'Màn hình 14 inch', queryParams: { category: 'laptop', q: '14-inch' } },
          { label: 'Màn hình 15.6 inch', queryParams: { category: 'laptop', q: '15-inch' } },
        ],
      },
    ],
  },
  {
    label: 'Laptop Gaming',
    slug: 'laptop-gaming',
    icon: Laptop,
    children: [
      {
        title: 'Thương hiệu',
        items: ['ASUS ROG / TUF', 'ACER Predator / Nitro', 'MSI Gaming', 'LENOVO Legion', 'DELL Gaming'].map((b) =>
          brandItem('laptop-gaming', b),
        ),
      },
      {
        title: 'Mức giá',
        items: [
          priceItem('laptop-gaming', 'Dưới 20 triệu', undefined, 20_000_000),
          priceItem('laptop-gaming', '20 - 25 triệu', 20_000_000, 25_000_000),
          priceItem('laptop-gaming', '25 - 35 triệu', 25_000_000, 35_000_000),
          priceItem('laptop-gaming', 'Trên 35 triệu', 35_000_000, undefined),
        ],
      },
      {
        title: 'Card đồ họa (VGA)',
        items: [
          { label: 'GeForce RTX 4050', queryParams: { category: 'laptop-gaming', q: 'rtx-4050' } },
          { label: 'GeForce RTX 4060', queryParams: { category: 'laptop-gaming', q: 'rtx-4060' } },
          { label: 'GeForce RTX 4070', queryParams: { category: 'laptop-gaming', q: 'rtx-4070' } },
          { label: 'GeForce RTX 4080 / 4090', queryParams: { category: 'laptop-gaming', q: 'rtx-4080' } },
        ],
      },
      {
        title: 'Màn hình Gaming',
        items: [
          { label: 'Tần số quét 144Hz', queryParams: { category: 'laptop-gaming', q: '144hz' } },
          { label: 'Tần số quét 165Hz - 240Hz', queryParams: { category: 'laptop-gaming', q: '240hz' } },
          { label: 'Độ phân giải 2K QHD', queryParams: { category: 'laptop-gaming', q: '2k' } },
          { label: 'Tấm nền OLED / IPS', queryParams: { category: 'laptop-gaming', q: 'oled' } },
        ],
      },
    ],
  },
  {
    label: 'PC GVN',
    slug: 'pc',
    icon: Monitor,
    children: [
      {
        title: 'Dòng máy',
        items: [
          { label: 'PC Gaming G-Master', queryParams: { category: 'pc', q: 'gaming' } },
          { label: 'PC Đồ họa - Render', queryParams: { category: 'pc', q: 'do-hoa' } },
          { label: 'PC Văn phòng - Doanh nghiệp', queryParams: { category: 'pc', q: 'van-phong' } },
          { label: 'PC Giả lập - AI Server', queryParams: { category: 'pc', q: 'server' } },
        ],
      },
      {
        title: 'Khoảng giá',
        items: [
          priceItem('pc', 'Dưới 10 triệu', undefined, 10_000_000),
          priceItem('pc', '10 - 20 triệu', 10_000_000, 20_000_000),
          priceItem('pc', '20 - 35 triệu', 20_000_000, 35_000_000),
          priceItem('pc', 'Trên 35 triệu', 35_000_000, undefined),
        ],
      },
      {
        title: 'CPU Vi xử lý',
        items: [
          { label: 'Intel Core i3', queryParams: { category: 'pc', q: 'core-i3' } },
          { label: 'Intel Core i5', queryParams: { category: 'pc', q: 'core-i5' } },
          { label: 'Intel Core i7 / i9', queryParams: { category: 'pc', q: 'core-i7' } },
          { label: 'AMD Ryzen 5 / 7', queryParams: { category: 'pc', q: 'ryzen' } },
        ],
      },
      {
        title: 'Card đồ họa VGA',
        items: [
          { label: 'RTX 3050 / 3060', queryParams: { category: 'pc', q: 'rtx-3050' } },
          { label: 'RTX 4060 / 4060 Ti', queryParams: { category: 'pc', q: 'rtx-4060' } },
          { label: 'RTX 4070 Super', queryParams: { category: 'pc', q: 'rtx-4070' } },
          { label: 'RTX 4080 Super / 4090', queryParams: { category: 'pc', q: 'rtx-4080' } },
        ],
      },
    ],
  },
  {
    label: 'Linh kiện',
    slug: 'linh-kien',
    icon: Cpu,
    children: [
      {
        title: 'CPU & Bo mạch',
        items: [
          { label: 'CPU Intel Gen 14', queryParams: { category: 'linh-kien', q: 'cpu-intel' } },
          { label: 'CPU AMD Ryzen 7000/9000', queryParams: { category: 'linh-kien', q: 'cpu-amd' } },
          { label: 'Mainboard ASUS ROG / TUF', queryParams: { category: 'linh-kien', q: 'main-asus' } },
          { label: 'Mainboard MSI / Gigabyte', queryParams: { category: 'linh-kien', q: 'main-msi' } },
        ],
      },
      {
        title: 'VGA - Card màn hình',
        items: [
          { label: 'NVIDIA RTX 4060 Series', queryParams: { category: 'linh-kien', q: 'rtx-4060' } },
          { label: 'NVIDIA RTX 4070 Series', queryParams: { category: 'linh-kien', q: 'rtx-4070' } },
          { label: 'NVIDIA RTX 4080 Series', queryParams: { category: 'linh-kien', q: 'rtx-4080' } },
          { label: 'AMD Radeon RX 7000', queryParams: { category: 'linh-kien', q: 'rx-7000' } },
        ],
      },
      {
        title: 'RAM & Ổ cứng SSD',
        items: [
          { label: 'RAM DDR4 16GB / 32GB', queryParams: { category: 'o-cung-ram', q: 'ram-ddr4' } },
          { label: 'RAM DDR5 RGB 6000MHz', queryParams: { category: 'o-cung-ram', q: 'ram-ddr5' } },
          { label: 'SSD NVMe PCIe Gen 4', queryParams: { category: 'o-cung-ram', q: 'ssd-gen4' } },
          { label: 'SSD Dung lượng 1TB / 2TB', queryParams: { category: 'o-cung-ram', q: 'ssd-1tb' } },
        ],
      },
      {
        title: 'Nguồn, Tản nhiệt, Case',
        items: [
          { label: 'Nguồn máy tính (PSU)', queryParams: { category: 'linh-kien', q: 'psu' } },
          { label: 'Tản nhiệt nước AIO', queryParams: { category: 'linh-kien', q: 'aio' } },
          { label: 'Tản nhiệt khí CPU', queryParams: { category: 'linh-kien', q: 'tan-khi' } },
          { label: 'Vỏ Case Gaming bể cá', queryParams: { category: 'linh-kien', q: 'case' } },
        ],
      },
    ],
  },
  {
    label: 'Màn hình',
    slug: 'man-hinh',
    icon: Tv,
    children: [
      {
        title: 'Thương hiệu',
        items: ['ASUS', 'LG', 'SAMSUNG', 'VIEWSONIC', 'AOC', 'DELL'].map((b) => brandItem('man-hinh', b)),
      },
      {
        title: 'Kích thước',
        items: [
          { label: 'Màn hình 24 inch', queryParams: { category: 'man-hinh', q: '24-inch' } },
          { label: 'Màn hình 27 inch', queryParams: { category: 'man-hinh', q: '27-inch' } },
          { label: 'Màn hình 32 inch', queryParams: { category: 'man-hinh', q: '32-inch' } },
          { label: 'Màn hình Cong Ultrawide', queryParams: { category: 'man-hinh', q: 'ultrawide' } },
        ],
      },
      {
        title: 'Tần số quét',
        items: [
          { label: '100Hz - 144Hz', queryParams: { category: 'man-hinh', q: '144hz' } },
          { label: '165Hz - 180Hz', queryParams: { category: 'man-hinh', q: '180hz' } },
          { label: '240Hz - 360Hz Esports', queryParams: { category: 'man-hinh', q: '240hz' } },
        ],
      },
      {
        title: 'Độ phân giải',
        items: [
          { label: 'Full HD (1920x1080)', queryParams: { category: 'man-hinh', q: 'fhd' } },
          { label: '2K QHD (2560x1440)', queryParams: { category: 'man-hinh', q: '2k' } },
          { label: '4K UHD (3840x2160)', queryParams: { category: 'man-hinh', q: '4k' } },
          { label: 'Tấm nền OLED cao cấp', queryParams: { category: 'man-hinh', q: 'oled' } },
        ],
      },
    ],
  },
];

@Component({
  selector: 'app-mega-menu',
  imports: [RouterLink, LucideAngularModule, CategorySidebarComponent],
  templateUrl: './mega-menu.component.html',
  styleUrl: './mega-menu.component.css',
})
export class MegaMenuComponent {
  readonly LayoutGrid = LayoutGrid;
  readonly ChevronDown = ChevronDown;
  readonly Wrench = Wrench;

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
