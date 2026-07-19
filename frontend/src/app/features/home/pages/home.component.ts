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
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private mock = inject(MockDataService);

  homeSections = this.mock.getHomeSections();
  promoCampaigns = this.mock.getPromoCampaigns();
  blogPosts = this.mock.getBlogPosts();
}
