import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HeroBannerComponent } from '../components/hero-banner.component';
import { GearVnBannerComponent } from '../components/gear-vn-banner.component';
import { FlashSaleSectionComponent } from '../components/flash-sale-section.component';
import { CategoryGridComponent } from '../components/category-grid.component';
import { ProductSectionComponent } from '../components/product-section.component';
import { MockDataService } from '@core/services/mock-data.service';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';

@Component({
  selector: 'app-home',
  imports: [
    HeroBannerComponent,
    GearVnBannerComponent,
    FlashSaleSectionComponent,
    CategoryGridComponent,
    ProductSectionComponent,
    RouterLink,
    NzIconModule,
    FallbackImageDirective,
  ],
  template: `
    <app-hero-banner />

    <app-gear-vn-banner />

    <app-flash-sale-section style="content-visibility: auto" />

    <app-category-grid style="content-visibility: auto" />

    @for (section of homeSections; track section.title) {
      <app-product-section style="content-visibility: auto" [section]="section" />
    }

    <!-- Promo campaigns -->
    <div class="max-w-screen-2xl mx-auto px-4 mt-8 lg:mt-10" style="content-visibility: auto">
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
        @for (campaign of promoCampaigns; track campaign.id) {
          <a
            [routerLink]="campaign.link"
            class="rounded-xl overflow-hidden border border-border/80 dark:border-border-dark/80 shadow-sm group"
          >
            <div class="relative">
              <img [src]="campaign.imageUrl" loading="lazy" appFallbackImage alt="{{ campaign.title }}" class="w-full aspect-[7/5] object-cover group-hover:scale-105 transition-transform duration-500" />
              <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-3">
                <p class="text-white font-bold text-xs sm:text-sm">{{ campaign.title }}</p>
                @if (campaign.subtitle) {
                  <p class="text-primary text-xs font-medium">{{ campaign.subtitle }}</p>
                }
              </div>
            </div>
          </a>
        }
      </div>
    </div>

    <!-- Blog posts -->
    <div class="max-w-screen-2xl mx-auto px-4 mt-8 lg:mt-10 mb-10 lg:mb-14" style="content-visibility: auto">
      <div class="flex items-center justify-between mb-5">
        <h2 class="text-lg lg:text-xl font-bold tracking-tight">Tin công nghệ</h2>
        <a routerLink="/" class="text-sm text-muted dark:text-muted-dark hover:text-primary font-medium transition-colors">
          Xem tất cả →
        </a>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        @for (post of blogPosts; track post.id) {
          <a
            routerLink="/"
            class="group rounded-xl border border-border/80 dark:border-border-dark/80 overflow-hidden shadow-sm hover:shadow-md transition-all duration-300 bg-surface dark:bg-surface-dark"
          >
            <div class="aspect-[16/9] bg-bg dark:bg-bg-dark overflow-hidden">
              <img [src]="post.imageUrl" loading="lazy" appFallbackImage alt="{{ post.title }}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
            </div>
            <div class="p-3.5">
              <p class="text-xs text-primary font-medium mb-1.5">{{ post.category }} · {{ post.date }}</p>
              <h3 class="text-sm font-semibold line-clamp-2 group-hover:text-primary transition-colors leading-snug">{{ post.title }}</h3>
              <p class="text-xs text-muted dark:text-muted-dark mt-1.5 line-clamp-2">{{ post.excerpt }}</p>
            </div>
          </a>
        }
      </div>
    </div>
  `,
})
export class HomeComponent {
  private mock = inject(MockDataService);

  homeSections = this.mock.getHomeSections();
  promoCampaigns = this.mock.getPromoCampaigns();
  blogPosts = this.mock.getBlogPosts();
}
