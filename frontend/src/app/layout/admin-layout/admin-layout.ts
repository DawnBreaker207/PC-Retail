import { Component, ChangeDetectionStrategy, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NzContentComponent, NzLayoutModule } from 'ng-zorro-antd/layout';
import { AdminSidebarComponent } from './sidebar/admin-sidebar.component';
import { AdminNavbarComponent } from './navbar/admin-navbar.component';

@Component({
  selector: 'app-admin-layout',
  imports: [NzContentComponent, NzLayoutModule, RouterOutlet, AdminSidebarComponent, AdminNavbarComponent],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLayout implements OnInit {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  isCollapsed = false;
  pageLabel = signal('Dashboard');

  private static readonly LABELS: Record<string, string> = {
    '': 'Dashboard',
    account: 'Tài khoản của tôi',
    orders: 'Đơn hàng',
    returns: 'Trả hàng',
    products: 'Sản phẩm',
    inventory: 'Kho hàng',
    categories: 'Danh mục',
    vouchers: 'Khuyến mãi',
    reviews: 'Đánh giá',
    users: 'Người dùng',
    'audit-log': 'Audit Log',
  };

  ngOnInit() {
    this.updatePageLabel();
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.updatePageLabel());
  }

  toggleCollapse(collapsed: boolean) {
    this.isCollapsed = collapsed;
  }

  private updatePageLabel() {
    const segment = this.activatedRoute.snapshot.firstChild?.url[0]?.path ?? '';
    this.pageLabel.set(AdminLayout.LABELS[segment] ?? 'Dashboard');
  }
}
