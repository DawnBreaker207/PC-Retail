import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-blog-detail',
  imports: [RouterLink, NzBreadCrumbModule, FallbackImageDirective],
  templateUrl: './blog-detail.component.html',
})
export class BlogDetailComponent {
  private route = inject(ActivatedRoute);
  private mock = inject(MockDataService);

  private allPosts = this.mock.getBlogPosts();
  slug = signal('');

  post = computed(() => this.allPosts.find((p) => p.slug === this.slug()) || null);
  paragraphs = computed(() => this.post()?.content.split('\n\n') ?? []);
  relatedPosts = computed(() =>
    this.allPosts.filter((p) => p.slug !== this.slug()).slice(0, 4),
  );

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.slug.set(params.get('slug') || '');
    });
  }
}
