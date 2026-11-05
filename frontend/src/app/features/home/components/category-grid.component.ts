import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-grid',
  imports: [RouterLink, FallbackImageDirective],
  templateUrl: './category-grid.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './category-grid.component.css',
})
export class CategoryGridComponent {
  private mock = inject(MockDataService);
  categories = this.mock.categories;
}
