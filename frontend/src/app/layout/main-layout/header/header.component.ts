import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { ThemeService } from '@core/services/theme.service';
import { AuthService } from '@core/auth/auth.service';
import { CartStore } from '@features/cart/store/cart.store';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';
import { MockDataService } from '@core/services/mock-data.service';
import { NotificationBellComponent } from '@shared/components/notification-bell.component';
import { NotificationService } from '@core/services/notification.service';
import { RealtimeService } from '@core/services/realtime.service';

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
    NzToolTipModule,
    NotificationBellComponent,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private router = inject(Router);
  private themeService = inject(ThemeService);
  private authService = inject(AuthService);
  private cartStore = inject(CartStore);
  private wishlistStore = inject(WishlistStore);
  private mock = inject(MockDataService);
  private notif = inject(NotificationService);
  private realtime = inject(RealtimeService);

  constructor() {
    // Đồng bộ server mỗi khi trạng thái login đổi (login sau khi header đã init)
    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.notif.load();
        this.wishlistStore.syncFromServer();
        this.realtime.connect();
      } else {
        this.realtime.disconnect();
      }
    });
  }

  isDark = this.themeService.isDark;
  cartCount = this.cartStore.totalCount;
  wishlistCount = this.wishlistStore.count;
  isLoggedIn = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;
  isAdminOrStaff = computed(() => {
    const role = this.currentUser()?.systemRole;
    return role === 'ADMIN' || role === 'STAFF' || role === 'SUPER_ADMIN';
  });
  showMobileSearch = signal(false);
  showMobileMenu = signal(false);

  toggleTheme() {
    this.themeService.toggle();
  }

  get userInitials(): string {
    const name = this.currentUser()?.fullName;
    if (!name) return 'U';
    return name.split(' ').map((s) => s[0]).join('').toUpperCase().slice(0, 2);
  }

  get userName(): string {
    return this.currentUser()?.fullName || 'Người dùng';
  }

  get userEmail(): string {
    return this.currentUser()?.email || '';
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

  categories = this.mock.categories;
}
