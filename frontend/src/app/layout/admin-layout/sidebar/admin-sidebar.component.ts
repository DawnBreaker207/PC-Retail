import { Component, ChangeDetectionStrategy, inject, input, output, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { animate, style, transition, trigger } from '@angular/animations';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMenuDirective, NzMenuItemComponent } from 'ng-zorro-antd/menu';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { AuthService } from '@core/auth/auth.service';
import { AdminWorkQueueService } from '@core/services/admin-work-queue.service';

export interface AdminNavItem {
  label: string;
  icon: string;
  route: string;
  badgeKey?: 'returns' | 'orders';
}

export interface AdminNavSection {
  title: string;
  items: AdminNavItem[];
}

@Component({
  selector: 'app-admin-sidebar',
  imports: [
    RouterLink, NzIconModule, NzMenuDirective, NzMenuItemComponent, NzLayoutModule, NzToolTipModule, NzBadgeModule,
  ],
  templateUrl: './admin-sidebar.component.html',
  styleUrl: './admin-sidebar.component.css',
  animations: [
    trigger('iconFade', [
      transition('open <=> closed', [
        style({ opacity: 0, transform: 'scale(0.6)' }),
        animate('220ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'scale(1)' })),
      ]),
    ]),
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSidebarComponent {
  private authService = inject(AuthService);
  private workQueue = inject(AdminWorkQueueService);
  private router = inject(Router);

  isCollapsed = input<boolean>(false);
  triggerCollapsed = output<boolean>();

  private readonly currentUrl = signal(this.router.url);
  navSections: AdminNavSection[] = [];
  returnsPendingCount = signal(0);
  ordersPendingCount = signal(0);

  constructor() {
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((e) => this.currentUrl.set(e.urlAfterRedirects));

    const role = this.authService.currentUser()?.systemRole;
    if (role === 'STAFF') {
      this.navSections = [
        { title: 'Tổng quan', items: [{ label: 'Dashboard', icon: 'dashboard', route: '/admin' }] },
        {
          title: 'Bán hàng',
          items: [
            { label: 'Đơn hàng', icon: 'shopping-cart', route: '/admin/orders', badgeKey: 'orders' },
            { label: 'Trả hàng', icon: 'swap', route: '/admin/returns', badgeKey: 'returns' },
          ],
        },
      ];
    } else {
      this.navSections = [
        { title: 'Tổng quan', items: [{ label: 'Dashboard', icon: 'dashboard', route: '/admin' }] },
        {
          title: 'Bán hàng',
          items: [
            { label: 'Đơn hàng', icon: 'shopping-cart', route: '/admin/orders', badgeKey: 'orders' },
            { label: 'Trả hàng', icon: 'swap', route: '/admin/returns', badgeKey: 'returns' },
          ],
        },
        {
          title: 'Sản phẩm',
          items: [
            { label: 'Sản phẩm', icon: 'appstore', route: '/admin/products' },
            { label: 'Kho hàng', icon: 'inbox', route: '/admin/inventory' },
            { label: 'Danh mục', icon: 'folder', route: '/admin/categories' },
          ],
        },
        {
          title: 'Khuyến mãi & đánh giá',
          items: [
            { label: 'Khuyến mãi', icon: 'gift', route: '/admin/vouchers' },
            { label: 'Đánh giá', icon: 'star', route: '/admin/reviews' },
          ],
        },
        {
          title: 'Hệ thống',
          items: [
            { label: 'Người dùng', icon: 'team', route: '/admin/users' },
            { label: 'Audit Log', icon: 'file-search', route: '/admin/audit-log' },
          ],
        },
        {
          title: 'Mua hàng & kiểm kê',
          items: [
            { label: 'Báo cáo', icon: 'bar-chart', route: '/admin/reports' },
            { label: 'Mua hàng', icon: 'shopping', route: '/admin/purchasing' },
            { label: 'Kiểm kê', icon: 'container', route: '/admin/stocktake' },
          ],
        },
      ];
    }

    this.workQueue.getPendingReturnsCount().pipe(takeUntilDestroyed()).subscribe((c) => this.returnsPendingCount.set(c));
    this.workQueue.getPendingPaymentOrdersCount().pipe(takeUntilDestroyed()).subscribe((c) => this.ordersPendingCount.set(c));
  }

  isActive(path: string): boolean {
    return this.currentUrl().split('?')[0] === path;
  }

  badgeCount(item: AdminNavItem): number {
    if (item.badgeKey === 'returns') return this.returnsPendingCount();
    if (item.badgeKey === 'orders') return this.ordersPendingCount();
    return 0;
  }
}
