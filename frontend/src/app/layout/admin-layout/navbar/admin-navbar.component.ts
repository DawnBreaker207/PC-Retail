import { Component, ChangeDetectionStrategy, inject, input, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzMenuDirective, NzMenuItemComponent, NzMenuDividerDirective } from 'ng-zorro-antd/menu';
import { AuthService } from '@core/auth/auth.service';
import { ThemeService } from '@core/services/theme.service';
import { AdminWorkQueueService } from '@core/services/admin-work-queue.service';

@Component({
  selector: 'app-admin-navbar',
  imports: [
    RouterLink, NzBadgeModule, NzBreadCrumbModule, NzDropDownModule, NzLayoutModule,
    NzIconModule, NzToolTipModule, NzAvatarModule, NzMenuDirective, NzMenuItemComponent, NzMenuDividerDirective,
  ],
  templateUrl: './admin-navbar.component.html',
  styleUrl: './admin-navbar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNavbarComponent {
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);
  private workQueue = inject(AdminWorkQueueService);

  pageLabel = input<string>('Dashboard');
  isDark = this.themeService.isDark;

  private returnsPendingCount = signal(0);
  private ordersPendingCount = signal(0);
  notificationCount = computed(() => this.returnsPendingCount() + this.ordersPendingCount());

  constructor() {
    this.workQueue.getPendingReturnsCount().pipe(takeUntilDestroyed()).subscribe((c) => this.returnsPendingCount.set(c));
    this.workQueue.getPendingPaymentOrdersCount().pipe(takeUntilDestroyed()).subscribe((c) => this.ordersPendingCount.set(c));
  }

  get userInitials(): string {
    const user = this.authService.currentUser();
    if (!user?.fullName) return 'A';
    return user.fullName.split(' ').map((s) => s[0]).join('').toUpperCase().slice(0, 2);
  }

  get userName(): string {
    return this.authService.currentUser()?.fullName || 'Admin';
  }

  toggleTheme() {
    this.themeService.toggle();
  }

  logout() {
    this.authService.logout();
  }
}
