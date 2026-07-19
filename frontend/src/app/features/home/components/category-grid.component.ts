import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-grid',
  imports: [RouterLink, NzIconModule, FallbackImageDirective],
  templateUrl: './category-grid.component.html',
  styleUrl: './category-grid.component.css',
})
export class CategoryGridComponent {
  private mock = inject(MockDataService);
  categories = this.mock.categories;
}
