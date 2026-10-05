import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-blog-list',
  imports: [RouterLink, NzBreadCrumbModule, FallbackImageDirective],
  templateUrl: './blog-list.component.html',
})
export class BlogListComponent {
  private mock = inject(MockDataService);
  private fallback = this.mock.getBlogPosts();
  posts = signal(this.fallback);

  constructor() {
    inject(HttpClient).get<unknown[]>(ApiEndpoints.articles.base).pipe(
      catchError(() => of([])),
    ).subscribe((list) => {
      if (Array.isArray(list) && list.length) this.posts.set(list as never[]);
    });
  }
}
