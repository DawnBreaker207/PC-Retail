import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-sidebar',
  imports: [RouterLink, NzIconModule],
  templateUrl: './category-sidebar.component.html',
  styleUrl: './category-sidebar.component.css',
})
export class CategorySidebarComponent {
  private mock = inject(MockDataService);

  categories = this.mock.categories;
}
