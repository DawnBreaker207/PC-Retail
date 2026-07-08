import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';

interface NavItem {
  label: string;
  icon: string;
  link: string;
  children?: { label: string; link: string }[];
}

@Component({
  selector: 'app-sidebar-nav',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  template: `
    <nav class="w-[240px] shrink-0 bg-surface dark:bg-surface-dark rounded-xl border border-border/80 dark:border-border-dark/80 shadow-sm overflow-hidden">
      <div class="px-4 py-3 border-b border-border/60 dark:border-border-dark/60">
        <h3 class="text-xs font-semibold uppercase tracking-wider text-muted dark:text-muted-dark">Danh mục</h3>
      </div>
      <ul class="py-1">
        @for (item of navItems; track item.label) {
          <li class="relative group"
              (mouseenter)="hovered.set(item.label)"
              (mouseleave)="hovered.set(null)">
            <a [routerLink]="item.link"
               class="flex items-center gap-3 px-4 py-2.5 text-sm font-medium transition-colors hover:bg-bg dark:hover:bg-bg-dark hover:text-primary"
               [class.text-primary]="hovered() === item.label"
               [attr.aria-haspopup]="item.children?.length ? 'true' : null">
              <span nz-icon [nzType]="item.icon" nzTheme="outline" class="text-base w-5 text-center shrink-0"></span>
              <span class="flex-1 truncate">{{ item.label }}</span>
              @if (item.children?.length) {
                <span nz-icon nzType="right" nzTheme="outline" class="text-xs text-muted dark:text-muted-dark transition-transform group-hover:translate-x-0.5 group-focus-within:translate-x-0.5"></span>
              }
            </a>
            @if (item.children?.length) {
              <div class="absolute left-full top-0 ml-1 hidden group-hover:block group-focus-within:block z-50 min-w-[200px] max-w-[300px] bg-surface dark:bg-surface-dark rounded-xl border border-border/80 dark:border-border-dark/80 shadow-lg py-1">
                @for (child of item.children; track child.label) {
                  <a [routerLink]="child.link"
                     class="block px-4 py-2 text-sm font-medium transition-colors hover:bg-bg dark:hover:bg-bg-dark hover:text-primary">
                    {{ child.label }}
                  </a>
                }
              </div>
            }
          </li>
        }
      </ul>
    </nav>
  `,
})
export class SidebarNavComponent {
  hovered = signal<string | null>(null);

  navItems: NavItem[] = [
    { label: 'Laptop', icon: 'laptop', link: '/products?category=laptop', children: [
      { label: 'Laptop Gaming', link: '/products?category=laptop-gaming' },
      { label: 'Laptop Văn phòng', link: '/products?category=laptop' },
      { label: 'Laptop Đồ họa', link: '/products?category=laptop' },
      { label: 'ASUS', link: '/products?category=laptop-asus' },
      { label: 'ACER', link: '/products?category=laptop-acer' },
      { label: 'MSI', link: '/products?category=laptop-msi' },
      { label: 'LENOVO', link: '/products?category=laptop-lenovo' },
      { label: 'DELL', link: '/products?category=laptop-dell' },
      { label: 'Dưới 15 triệu', link: '/products?category=laptop-duoi-15tr' },
      { label: '15 - 25 triệu', link: '/products?category=laptop-15-25tr' },
      { label: '25 - 40 triệu', link: '/products?category=laptop-25-40tr' },
      { label: 'Trên 40 triệu', link: '/products?category=laptop-tren-40tr' },
    ]},
    { label: 'Laptop Gaming', icon: 'play-circle', link: '/products?category=laptop-gaming', children: [
      { label: 'ASUS ROG', link: '/products?category=rog' },
      { label: 'MSI Gaming', link: '/products?category=msi-gaming' },
      { label: 'ACER Predator', link: '/products?category=predator' },
      { label: 'LENOVO Legion', link: '/products?category=legion' },
      { label: 'Dưới 20tr', link: '/products?category=gaming-duoi-20tr' },
      { label: '20 - 30tr', link: '/products?category=gaming-20-30tr' },
      { label: '30 - 50tr', link: '/products?category=gaming-30-50tr' },
      { label: 'Trên 50tr', link: '/products?category=gaming-tren-50tr' },
    ]},
    { label: 'PC - Máy Tính', icon: 'desktop', link: '/products?category=pc', children: [
      { label: 'PC Gaming', link: '/products?category=pc-gaming' },
      { label: 'PC Workstation', link: '/products?category=pc-workstation' },
      { label: 'PC Văn phòng', link: '/products?category=pc-van-phong' },
      { label: 'PC Đồ họa', link: '/products?category=pc-do-hoa' },
    ]},
    { label: 'Màn Hình', icon: 'monitor', link: '/products?category=man-hinh', children: [
      { label: 'Màn hình Gaming', link: '/products?category=man-hinh' },
      { label: 'Màn hình Văn phòng', link: '/products?category=man-hinh' },
      { label: 'Màn hình 4K', link: '/products?category=man-hinh' },
    ]},
    { label: 'Bàn Phím', icon: 'keyboard', link: '/products?category=ban-phim' },
    { label: 'Chuột', icon: 'scan', link: '/products?category=chuot' },
    { label: 'Tai Nghe', icon: 'customer-service', link: '/products?category=tai-nghe' },
    { label: 'Linh Kiện', icon: 'tool', link: '/products?category=linh-kien', children: [
      { label: 'CPU', link: '/products?category=linh-kien' },
      { label: 'VGA', link: '/products?category=linh-kien' },
      { label: 'Mainboard', link: '/products?category=linh-kien' },
      { label: 'RAM', link: '/products?category=linh-kien' },
      { label: 'Ổ Cứng SSD/HDD', link: '/products?category=o-cung-ram' },
    ]},
    { label: 'Ổ Cứng, RAM', icon: 'database', link: '/products?category=o-cung-ram' },
    { label: 'Loa, Micro', icon: 'sound', link: '/products?category=loa-micro-webcam' },
    { label: 'Ghế - Bàn', icon: 'rest', link: '/products?category=ghe-ban' },
    { label: 'Phụ Kiện', icon: 'appstore', link: '/products?category=phu-kien', children: [
      { label: 'Hub, Sạc, Cáp', link: '/products?category=phu-kien' },
      { label: 'Lót chuột', link: '/products?category=chuot' },
      { label: 'Webcam', link: '/products?category=loa-micro-webcam' },
    ]},
    { label: 'Handheld, Console', icon: 'gamepad', link: '/products?category=handheld-console' },
    { label: 'Phần Mềm, Mạng', icon: 'wifi', link: '/products?category=phan-mem-mang' },
  ];
}
