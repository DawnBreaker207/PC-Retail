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
  template: `
    <nz-layout class="min-h-screen">
      <nz-sider
        nzCollapsible
        [(nzCollapsed)]="collapsed"
        [nzWidth]="250"
        [nzCollapsedWidth]="64"
        class="!fixed !left-0 !top-0 !bottom-0 !z-40"
      >
        <div class="h-16 flex items-center justify-center border-b border-border/20">
          @if (!collapsed()) {
            <a routerLink="/admin" class="text-white font-bold text-lg tracking-tight">PC Retail</a>
          } @else {
            <a routerLink="/admin" class="text-white font-bold text-lg">P</a>
          }
        </div>
        <ul nz-menu nzTheme="dark" nzMode="inline" class="!border-r-0">
          @for (item of navItems; track item.route) {
            <li nz-menu-item [routerLink]="item.route">
              <span nz-icon [nzType]="item.icon" nzTheme="outline"></span>
              <span>{{ item.label }}</span>
            </li>
          }
        </ul>
      </nz-sider>

      <nz-layout [style.marginLeft.px]="collapsed() ? 64 : 250" class="!transition-all !duration-200">
        <nz-header class="!bg-surface dark:!bg-surface-dark !h-16 !px-6 !flex !items-center !justify-between !border-b !border-border/80 !sticky !top-0 !z-30">
          <button nz-button nzType="text" (click)="toggleCollapsed()" class="!text-lg">
            <span nz-icon [nzType]="collapsed() ? 'menu-unfold' : 'menu-fold'"></span>
          </button>

          <div class="flex items-center gap-3">
            <button nz-button nzType="text" (click)="toggleTheme()">
              <span nz-icon [nzType]="isDark() ? 'sun' : 'moon'"></span>
            </button>
            <div nz-dropdown [nzDropdownMenu]="userMenu" nzPlacement="bottomRight">
              <div class="flex items-center gap-2 cursor-pointer">
                <nz-avatar [nzText]="userInitials" nzSize="small" class="!bg-primary"></nz-avatar>
                <span class="text-sm font-medium hidden sm:inline">{{ userName }}</span>
              </div>
            </div>
            <nz-dropdown-menu #userMenu="nzDropdownMenu">
              <ul nz-menu>
                <li nz-menu-item routerLink="/admin"><span nz-icon nzType="dashboard"></span> Dashboard</li>
                <li nz-menu-divider></li>
                <li nz-menu-item (click)="logout()" (keydown.enter)="logout()" tabindex="0"><span nz-icon nzType="logout"></span> Đăng xuất</li>
              </ul>
            </nz-dropdown-menu>
          </div>
        </nz-header>

        <nz-content class="!p-6">
          <router-outlet />
        </nz-content>
      </nz-layout>
    </nz-layout>
  `,
})
export class AdminLayout {
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);

  collapsed = signal(false);
  isDark = this.themeService.isDark;

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/admin' },
    { label: 'Đơn hàng', icon: 'shopping-cart', route: '/admin/orders' },
    { label: 'Sản phẩm', icon: 'appstore', route: '/admin/products' },
    { label: 'Kho hàng', icon: 'inbox', route: '/admin/inventory' },
    { label: 'Danh mục', icon: 'folder', route: '/admin/categories' },
    { label: 'Khuyến mãi', icon: 'gift', route: '/admin/vouchers' },
    { label: 'Người dùng', icon: 'team', route: '/admin/users' },
  ];

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
