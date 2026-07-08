import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-category-strip',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  template: `
    <div class="grid grid-cols-2 md:grid-cols-4 auto-rows-fr gap-4">
      @for (cat of categories; track cat.label) {
        <a [routerLink]="cat.link"
           class="flex flex-col items-center justify-center gap-3 bg-surface dark:bg-surface-dark rounded-xl border border-border/80 dark:border-border-dark/80 shadow-sm p-6 hover:shadow-md hover:border-primary/30 transition-all group min-h-[240px]">
          <div class="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center shrink-0 group-hover:bg-primary/20 transition-colors">
            <span nz-icon [nzType]="cat.icon" nzTheme="outline" class="text-primary text-xl"></span>
          </div>
          <div class="text-center">
            <p class="text-sm font-semibold leading-tight">{{ cat.label }}</p>
            <p class="text-xs text-muted dark:text-muted-dark mt-1">{{ cat.count }} sản phẩm</p>
          </div>
          <span nz-icon nzType="right" nzTheme="outline" class="text-xs text-muted dark:text-muted-dark group-hover:text-primary transition-colors"></span>
        </a>
      }
    </div>
  `,
})
export class CategoryStripComponent {
  categories = [
    { label: 'Laptop Gaming', icon: 'laptop', count: '128', link: '/products?category=laptop-gaming' },
    { label: 'PC Build Sẵn', icon: 'desktop', count: '64', link: '/products?category=pc' },
    { label: 'Bàn Phím Cơ', icon: 'keyboard', count: '96', link: '/products?category=ban-phim' },
    { label: 'Màn Hình', icon: 'monitor', count: '72', link: '/products?category=man-hinh' },
  ];
}
