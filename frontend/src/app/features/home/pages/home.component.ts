import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HeroBannerComponent } from '../components/hero-banner.component';
import { FlashSaleSectionComponent } from '../components/flash-sale-section.component';
import { CategoryGridComponent } from '../components/category-grid.component';
import { ProductSectionComponent } from '../components/product-section.component';
import { MockDataService } from '@core/services/mock-data.service';
import { ArticleService } from '@core/services/article.service';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { BlogPost } from '@core/models/common.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    HeroBannerComponent,
    FlashSaleSectionComponent,
    CategoryGridComponent,
    ProductSectionComponent,
    RouterLink,
    FallbackImageDirective,
  ],
  templateUrl: './home.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private mock = inject(MockDataService);
  private articleService = inject(ArticleService);

  homeSections = this.mock.getHomeSections();
  promoCampaigns = this.mock.getPromoCampaigns();
  blogPosts = signal<BlogPost[]>(this.mock.getBlogPosts());

  constructor() {
    this.articleService.getArticles().subscribe((articles) => {
      if (articles && articles.length > 0) {
        this.blogPosts.set(articles);
      }
    });
  }
}
