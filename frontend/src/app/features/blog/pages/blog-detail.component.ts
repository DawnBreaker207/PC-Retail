import { Component, computed, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ArticleService } from '@core/services/article.service';
import { BlogPost } from '@core/models/common.model';

@Component({
  selector: 'app-blog-detail',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, FallbackImageDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './blog-detail.component.html',
})
export class BlogDetailComponent {
  readonly ChevronRight = ChevronRight;
  private route = inject(ActivatedRoute);
  private articleService = inject(ArticleService);

  post = signal<BlogPost | null>(null);
  allPosts = signal<BlogPost[]>([]);
  slug = signal('');

  paragraphs = computed(() => this.post()?.content.split('\n\n') ?? []);
  relatedPosts = computed(() =>
    this.allPosts()
      .filter((p) => p.slug !== this.slug())
      .slice(0, 4),
  );

  constructor() {
    this.articleService.getArticles().subscribe((list) => {
      this.allPosts.set(list);
    });

    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const s = params.get('slug') || '';
      this.slug.set(s);
      if (s) {
        this.articleService.getArticleBySlug(s).subscribe((p) => {
          this.post.set(p);
        });
      }
    });
  }
}
