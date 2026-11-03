import { Component, ChangeDetectionStrategy, inject, input, output, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { AuthService } from '@core/auth/auth.service';
import { AdminWorkQueueService } from '@core/services/admin-work-queue.service';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import {
  LucideAngularModule,
  LayoutDashboard,
  ShoppingCart,
  RotateCcw,
  Package,
  Warehouse,
  FolderTree,
  Ticket,
  Star,
  Users,
  FileText,
  ChartColumn,
  ShoppingBag,
  Boxes,
  ChevronLeft,
  ChevronRight,
  Zap
} from 'lucide-angular';

export interface AdminNavItem {
  label: string;
  icon: any;
  route: string;
  badgeKey?: 'returns' | 'orders';
}

export interface AdminNavSection {
  title: string;
  items: AdminNavItem[];
}

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [
    NgClass,
    RouterLink,
    HlmBadgeDirective,
    LucideAngularModule,
  ],
  templateUrl: './admin-sidebar.component.html',
  styleUrl: './admin-sidebar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSidebarComponent {
  readonly Zap = Zap;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;

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
        { title: 'Tổng quan', items: [{ label: 'Dashboard', icon: LayoutDashboard, route: '/admin' }] },
        {
          title: 'Bán hàng',
          items: [
            { label: 'Đơn hàng', icon: ShoppingCart, route: '/admin/orders', badgeKey: 'orders' },
            { label: 'Trả hàng', icon: RotateCcw, route: '/admin/returns', badgeKey: 'returns' },
          ],
        },
      ];
    } else {
      this.navSections = [
        { title: 'Tổng quan', items: [{ label: 'Dashboard', icon: LayoutDashboard, route: '/admin' }] },
        {
          title: 'Bán hàng',
          items: [
            { label: 'Đơn hàng', icon: ShoppingCart, route: '/admin/orders', badgeKey: 'orders' },
            { label: 'Trả hàng', icon: RotateCcw, route: '/admin/returns', badgeKey: 'returns' },
          ],
        },
        {
          title: 'Sản phẩm',
          items: [
            { label: 'Sản phẩm', icon: Package, route: '/admin/products' },
            { label: 'Kho hàng', icon: Warehouse, route: '/admin/inventory' },
            { label: 'Danh mục', icon: FolderTree, route: '/admin/categories' },
          ],
        },
        {
          title: 'Khuyến mãi & đánh giá',
          items: [
            { label: 'Khuyến mãi', icon: Ticket, route: '/admin/vouchers' },
            { label: 'Đánh giá', icon: Star, route: '/admin/reviews' },
          ],
        },
        {
          title: 'Hệ thống',
          items: [
            { label: 'Người dùng', icon: Users, route: '/admin/users' },
            { label: 'Audit Log', icon: FileText, route: '/admin/audit-log' },
          ],
        },
        {
          title: 'Mua hàng & kiểm kê',
          items: [
            { label: 'Báo cáo', icon: ChartColumn, route: '/admin/reports' },
            { label: 'Mua hàng', icon: ShoppingBag, route: '/admin/purchasing' },
            { label: 'Kiểm kê', icon: Boxes, route: '/admin/stocktake' },
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

  toggleCollapse() {
    this.triggerCollapsed.emit(!this.isCollapsed());
  }
}
