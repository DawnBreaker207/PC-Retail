import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMenuDirective, NzMenuItemComponent, NzMenuDividerDirective } from 'ng-zorro-antd/menu';
import { AuthService } from '@core/auth/auth.service';
import { ThemeService } from '@core/services/theme.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-admin-layout',
  imports: [
    RouterOutlet, RouterLink,
    NzLayoutModule, NzIconModule, NzAvatarModule, NzDropDownModule, NzButtonModule,
    NzMenuDirective, NzMenuItemComponent, NzMenuDividerDirective,
  ],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);

  collapsed = signal(false);
  isDark = this.themeService.isDark;

  navItems: NavItem[] = [];

  constructor() {
    const role = this.authService.currentUser()?.systemRole;
    if (role === 'STAFF') {
      this.navItems = [
        { label: 'Dashboard', icon: 'dashboard', route: '/admin' },
        { label: 'Đơn hàng', icon: 'shopping-cart', route: '/admin/orders' },
        { label: 'Trả hàng', icon: 'swap', route: '/admin/returns' },
      ];
    } else {
      this.navItems = [
        { label: 'Dashboard', icon: 'dashboard', route: '/admin' },
        { label: 'Đơn hàng', icon: 'shopping-cart', route: '/admin/orders' },
        { label: 'Sản phẩm', icon: 'appstore', route: '/admin/products' },
        { label: 'Kho hàng', icon: 'inbox', route: '/admin/inventory' },
        { label: 'Danh mục', icon: 'folder', route: '/admin/categories' },
        { label: 'Khuyến mãi', icon: 'gift', route: '/admin/vouchers' },
        { label: 'Người dùng', icon: 'team', route: '/admin/users' },
      ];
    }
  }

  get userInitials(): string {
    const user = this.authService.currentUser();
    if (!user?.fullName) return 'A';
    return user.fullName.split(' ').map((s) => s[0]).join('').toUpperCase().slice(0, 2);
  }

  get userName(): string {
    return this.authService.currentUser()?.fullName || 'Admin';
  }

  toggleCollapsed() {
    this.collapsed.update((v) => !v);
  }

  toggleTheme() {
    this.themeService.toggle();
  }

  logout() {
    this.authService.logout();
  }
}
