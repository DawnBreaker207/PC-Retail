import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ArticleService } from '@core/services/article.service';
import { BlogPost } from '@core/models/common.model';

@Component({
  selector: 'app-blog-list',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, FallbackImageDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './blog-list.component.html',
})
export class BlogListComponent {
  readonly ChevronRight = ChevronRight;
  private articleService = inject(ArticleService);
  posts = signal<BlogPost[]>([]);

  constructor() {
    this.articleService.getArticles().subscribe((list) => {
      this.posts.set(list);
    });
  }
}
