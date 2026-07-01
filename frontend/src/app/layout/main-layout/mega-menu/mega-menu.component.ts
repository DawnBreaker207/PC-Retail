import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { CategoryService, Category } from '@core/services/category.service';

interface MenuGroup {
  label: string;
  slug: string;
  icon: string;
  children?: { title: string; items: { label: string; slug: string }[] }[];
}

const DEFAULT_GROUPS: MenuGroup[] = [
  {
    label: 'Laptop',
    slug: 'laptop',
    icon: 'laptop',
    children: [
      {
        title: 'Thương hiệu',
        items: [
          { label: 'ASUS', slug: 'laptop-asus' },
          { label: 'ACER', slug: 'laptop-acer' },
          { label: 'MSI', slug: 'laptop-msi' },
          { label: 'LENOVO', slug: 'laptop-lenovo' },
          { label: 'DELL', slug: 'laptop-dell' },
        ],
      },
      {
        title: 'Giá bán',
        items: [
          { label: 'Dưới 15 triệu', slug: 'laptop-duoi-15tr' },
          { label: '15 - 25 triệu', slug: 'laptop-15-25tr' },
          { label: '25 - 40 triệu', slug: 'laptop-25-40tr' },
          { label: 'Trên 40 triệu', slug: 'laptop-tren-40tr' },
        ],
      },
      {
        title: 'Dòng phổ biến',
        items: [
          { label: 'ASUS ROG', slug: 'asus-rog' },
          { label: 'ASUS TUF', slug: 'asus-tuf' },
          { label: 'MSI Gaming', slug: 'msi-gaming' },
          { label: 'ACER Predator', slug: 'acer-predator' },
        ],
      },
      {
        title: 'CPU',
        items: [
          { label: 'Intel Core i5', slug: 'laptop-i5' },
          { label: 'Intel Core i7', slug: 'laptop-i7' },
          { label: 'Intel Core i9', slug: 'laptop-i9' },
          { label: 'AMD Ryzen', slug: 'laptop-ryzen' },
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
        items: [
          { label: 'ASUS ROG', slug: 'rog' },
          { label: 'MSI Gaming', slug: 'msi-gaming' },
          { label: 'ACER Predator', slug: 'predator' },
          { label: 'LENOVO Legion', slug: 'legion' },
        ],
      },
      {
        title: 'Giá bán',
        items: [
          { label: 'Dưới 20tr', slug: 'gaming-duoi-20tr' },
          { label: '20-30tr', slug: 'gaming-20-30tr' },
          { label: '30-50tr', slug: 'gaming-30-50tr' },
          { label: 'Trên 50tr', slug: 'gaming-tren-50tr' },
        ],
      },
    ],
  },
  {
    label: 'PC',
    slug: 'pc',
    icon: 'desktop',
    children: [
      {
        title: 'Dòng PC',
        items: [
          { label: 'PC Gaming', slug: 'pc-gaming' },
          { label: 'PC Workstation', slug: 'pc-workstation' },
          { label: 'PC Văn phòng', slug: 'pc-van-phong' },
          { label: 'PC Đồ họa', slug: 'pc-do-hoa' },
        ],
      },
      {
        title: 'Thương hiệu',
        items: [
          { label: 'PC GVN', slug: 'pc-gvn' },
          { label: 'Build sẵn', slug: 'pc-build-san' },
        ],
      },
    ],
  },
  {
    label: 'Linh kiện',
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
    icon: 'tags',
  },
];

@Component({
  selector: 'app-mega-menu',
  imports: [RouterLink, NzMenuModule, NzIconModule],
  templateUrl: './mega-menu.component.html',
  styleUrl: './mega-menu.component.css',
})
export class MegaMenuComponent {
  private categoryService = inject(CategoryService);
  private destroyRef = inject(DestroyRef);

  categories = signal<MenuGroup[]>(DEFAULT_GROUPS);

  constructor() {
    this.categoryService.categories$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((cats) => {
      if (cats.length > 0) {
        const apiGroups: MenuGroup[] = cats
          .filter((c) => c.parentId == null)
          .slice(0, 6)
          .map((c) => ({
            label: c.name,
            slug: c.slug,
            icon: this.getIconForSlug(c.slug),
            children: this.buildSubMenu(cats, c.id),
          }));
        this.categories.set(apiGroups);
      }
    });
  }

  private buildSubMenu(all: Category[], parentId: number) {
    const subs = all.filter((c) => c.parentId === parentId);
    if (subs.length === 0) return undefined;
    return [
      {
        title: 'Danh mục',
        items: subs.map((c) => ({ label: c.name, slug: c.slug })),
      },
    ];
  }

  private getIconForSlug(slug: string): string {
    const map: Record<string, string> = {
      laptop: 'laptop',
      'laptop-gaming': 'play-circle',
      pc: 'desktop',
      'linh-kien': 'tool',
      'man-hinh': 'monitor',
      'phu-kien': 'tags',
    };
    return map[slug] || 'appstore';
  }
}
