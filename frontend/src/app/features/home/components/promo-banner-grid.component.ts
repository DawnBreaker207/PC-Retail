import { Component } from '@angular/core';
import { PromoCardComponent } from './promo-card.component';

@Component({
  selector: 'app-promo-banner-grid',
  standalone: true,
  imports: [PromoCardComponent],
  templateUrl: './promo-banner-grid.component.html',
  styleUrl: './promo-banner-grid.component.css',
})
export class PromoBannerGridComponent {
  banners = [
    {
      id: 1,
      title: 'FLASH SALE DEAL GẤP',
      imageUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80',
      link: '/products?sort=flash-sale',
      bgColor: 'linear-gradient(135deg, var(--color-error), var(--gradient-error-deep))',
      priceText: 'Giảm đến 40%',
      badgeText: 'HOT DEAL',
    },
    {
      id: 2,
      title: 'Màn Hình Gaming OLED',
      imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=man-hinh',
      bgColor: 'linear-gradient(135deg, var(--color-info), var(--gradient-info-deep))',
      priceText: 'Giảm 35%',
      badgeText: '240HZ',
    },
    {
      id: 3,
      title: 'Chuột Không Dây Siêu Nhẹ',
      imageUrl: 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=chuot',
      bgColor: 'linear-gradient(135deg, var(--color-warning), var(--gradient-warning-deep))',
      priceText: 'Chỉ từ 490K',
      badgeText: 'ESPORTS',
    },
    {
      id: 4,
      title: 'Build PC Core Ultra 9',
      imageUrl: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=500&auto=format&fit=crop&q=80',
      link: '/pc-builder/custom',
      bgColor: 'linear-gradient(135deg, var(--color-purple), var(--gradient-purple-deep))',
      priceText: 'Tặng AIO 360',
      badgeText: 'HI-END',
    },
    {
      id: 5,
      title: 'Tai Nghe Gaming 7.1',
      imageUrl: 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=tai-nghe',
      bgColor: 'linear-gradient(135deg, var(--color-cyan), var(--gradient-cyan-deep))',
      priceText: 'Giảm 25%',
      badgeText: 'SURROUND',
    },
    {
      id: 6,
      title: 'Bàn Phím Cơ Custom',
      imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=ban-phim',
      bgColor: 'linear-gradient(135deg, var(--color-pink), var(--gradient-pink-deep))',
      priceText: 'Hot-swap RGB',
      badgeText: 'MECHANICAL',
    },
  ];
}
