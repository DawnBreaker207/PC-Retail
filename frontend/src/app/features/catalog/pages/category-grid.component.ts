import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-grid',
  imports: [RouterLink, NzBreadCrumbModule, NzIconModule],
  templateUrl: './category-grid.component.html',
})
export class CategoryGridComponent {
  private mock = inject(MockDataService);

  categories = computed(() => {
    const products = this.mock.generateProducts();
    return this.mock.categories.map((cat) => ({
      ...cat,
      productCount: products.filter((p) => p.categoryName === cat.slug).length,
    }));
  });
}
