import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { ThemeService } from '@core/services/theme.service';
import { AuthService } from '@core/auth/auth.service';
import { CartStore } from '@features/cart/store/cart.store';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    NzBadgeModule,
    NzDropDownModule,
    NzAvatarModule,
    NzButtonModule,
    NzInputModule,
    NzIconModule,
    NzDrawerModule,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private router = inject(Router);
  private themeService = inject(ThemeService);
  private authService = inject(AuthService);
  private cartStore = inject(CartStore);

  isDark = this.themeService.isDark;
  cartCount = this.cartStore.totalCount;
  isLoggedIn = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;
  showMobileSearch = signal(false);
  showMobileMenu = signal(false);

  toggleTheme() {
    this.themeService.toggle();
  }

  search(query: string) {
    const q = query.trim();
    if (q) {
      this.router.navigate(['/products'], { queryParams: { q } });
    }
  }

  logout() {
    this.authService.logout();
  }

  categories = [
    { name: 'Laptop', slug: 'laptop' },
    { name: 'Laptop Gaming', slug: 'laptop-gaming' },
    { name: 'PC GVN', slug: 'pc' },
    { name: 'Main, CPU, VGA', slug: 'linh-kien' },
    { name: 'Case, Nguồn, Tản', slug: 'case-nguon-tan' },
    { name: 'Ổ cứng, RAM, Thẻ nhớ', slug: 'o-cung-ram' },
    { name: 'Loa, Micro, Webcam', slug: 'loa-micro-webcam' },
    { name: 'Màn hình', slug: 'man-hinh' },
    { name: 'Bàn phím', slug: 'ban-phim' },
    { name: 'Chuột + Lót chuột', slug: 'chuot' },
    { name: 'Tai Nghe', slug: 'tai-nghe' },
    { name: 'Ghế - Bàn', slug: 'ghe-ban' },
    { name: 'Phần mềm, mạng', slug: 'phan-mem-mang' },
    { name: 'Handheld, Console', slug: 'handheld-console' },
    { name: 'Phụ kiện (Hub, sạc, cáp)', slug: 'phu-kien' },
    { name: 'Dịch vụ và thông tin khác', slug: 'dich-vu' },
  ];
}
