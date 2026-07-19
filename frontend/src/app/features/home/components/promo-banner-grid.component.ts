import { Component, inject } from '@angular/core';
import { MockDataService } from '@core/services/mock-data.service';
import { PromoCardComponent } from './promo-card.component';

@Component({
  selector: 'app-promo-banner-grid',
  standalone: true,
  imports: [PromoCardComponent],
  templateUrl: './promo-banner-grid.component.html',
  styleUrl: './promo-banner-grid.component.css',
})
export class PromoBannerGridComponent {
  private mock = inject(MockDataService);

  banners = [
    { id: 1, title: 'FLASH SALE', imageUrl: this.mock.getHeroSlides()[0].image, link: '/products?sort=flash-sale', bgColor: 'linear-gradient(135deg, var(--color-error), var(--gradient-error-deep))', priceText: 'Giảm đến 40%', badgeText: 'Sale' },
    { id: 2, title: 'Màn Hình Máy Tính', imageUrl: this.mock.getHeroSlides()[3].image, link: '/products?category=man-hinh', bgColor: 'linear-gradient(135deg, var(--color-info), var(--gradient-info-deep))', priceText: 'Giảm đến 30%', badgeText: '' },
    { id: 3, title: 'Chuột Gaming', imageUrl: this.mock.getHeroSlides()[6].image, link: '/products?category=chuot', bgColor: 'linear-gradient(135deg, var(--color-warning), var(--gradient-warning-deep))', priceText: 'Giảm sốc', badgeText: '' },
    { id: 4, title: 'Build PC Cao Cấp', imageUrl: this.mock.getHeroSlides()[1].image, link: '/products?category=pc', bgColor: 'linear-gradient(135deg, var(--color-purple), var(--gradient-purple-deep))', priceText: 'Tặng màn OLED', badgeText: 'Hot' },
    { id: 5, title: 'Tai Nghe Không Dây', imageUrl: this.mock.getHeroSlides()[5].image, link: '/products?category=tai-nghe', bgColor: 'linear-gradient(135deg, var(--color-cyan), var(--gradient-cyan-deep))', priceText: 'Giảm 25%', badgeText: '' },
    { id: 6, title: 'Bàn Phím Cơ', imageUrl: this.mock.getHeroSlides()[4].image, link: '/products?category=ban-phim', bgColor: 'linear-gradient(135deg, var(--color-pink), var(--gradient-pink-deep))', priceText: 'Flash sale', badgeText: '' },
  ];
}
