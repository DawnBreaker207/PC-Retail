import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-category-strip',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  templateUrl: './category-strip.component.html',
  styleUrl: './category-strip.component.css',
})
export class CategoryStripComponent {
  categories = [
    { label: 'Laptop Gaming', icon: 'laptop', count: '128', link: '/products?category=laptop-gaming' },
    { label: 'PC Build Sẵn', icon: 'desktop', count: '64', link: '/products?category=pc' },
    { label: 'Bàn Phím Cơ', icon: 'tool', count: '96', link: '/products?category=ban-phim' },
    { label: 'Màn Hình', icon: 'monitor', count: '72', link: '/products?category=man-hinh' },
  ];
}
