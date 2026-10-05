import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MockDataService } from '@core/services/mock-data.service';

const STRIP_SLUGS = [
  { label: 'Laptop Gaming', icon: 'laptop', slug: 'laptop-gaming' },
  { label: 'PC GVN', icon: 'desktop', slug: 'pc' },
  { label: 'Linh Kiện', icon: 'tool', slug: 'linh-kien' },
  { label: 'Màn Hình', icon: 'monitor', slug: 'man-hinh' },
];

@Component({
  selector: 'app-category-strip',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  templateUrl: './category-strip.component.html',
  styleUrl: './category-strip.component.css',
})
export class CategoryStripComponent {
  private mock = inject(MockDataService);

  categories = (() => {
    const products = this.mock.generateProducts();
    return STRIP_SLUGS.map((s) => ({
      ...s,
      count: products.filter((p) => p.categoryName === s.slug).length,
    }));
  })();
}
