import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-category-grid',
  imports: [RouterLink, NzIconModule],
  template: `
    <div class="bg-surface dark:bg-surface-dark py-8 lg:py-10 mt-8 lg:mt-10 border-y border-border/60 dark:border-border-dark/60">
      <div class="max-w-screen-2xl mx-auto px-4">
        <div class="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 lg:grid-cols-10 xl:grid-cols-12 gap-1">
          @for (cat of categories; track cat.id) {
            <a
              [routerLink]="['/products']"
              [queryParams]="{ category: cat.slug }"
              class="flex flex-col items-center gap-1.5 p-2.5 sm:p-3 rounded-xl hover:bg-primary/5 dark:hover:bg-primary/10 transition-all duration-200 group"
            >
              <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-lg bg-bg dark:bg-bg-dark flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                <img [src]="cat.imageUrl" alt="{{ cat.name }}" class="w-8 h-8 sm:w-10 sm:h-10 object-contain" />
              </div>
              <span class="text-[10px] sm:text-xs text-center text-muted dark:text-muted-dark group-hover:text-primary leading-tight line-clamp-2 font-medium">{{ cat.name }}</span>
            </a>
          }
        </div>
      </div>
    </div>
  `,
})
export class CategoryGridComponent {
  private mock = inject(MockDataService);
  categories = this.mock.categories;
}
