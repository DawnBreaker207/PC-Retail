import { Component, ChangeDetectionStrategy, inject, input, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '@core/auth/auth.service';
import { ThemeService } from '@core/services/theme.service';
import { AdminWorkQueueService } from '@core/services/admin-work-queue.service';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import {
  LucideAngularModule,
  Bell,
  Sun,
  Moon,
  ChevronRight,
  User,
  LogOut,
  LayoutDashboard
} from 'lucide-angular';

@Component({
  selector: 'app-admin-navbar',
  standalone: true,
  imports: [
    RouterLink,
    HlmBadgeDirective,
    HlmButtonDirective,
    LucideAngularModule
  ],
  templateUrl: './admin-navbar.component.html',
  styleUrl: './admin-navbar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNavbarComponent {
  readonly Bell = Bell;
  readonly Sun = Sun;
  readonly Moon = Moon;
  readonly ChevronRight = ChevronRight;
  readonly User = User;
  readonly LogOut = LogOut;
  readonly LayoutDashboard = LayoutDashboard;

  private authService = inject(AuthService);
  private themeService = inject(ThemeService);
  private workQueue = inject(AdminWorkQueueService);

  pageLabel = input<string>('Dashboard');
  isDark = this.themeService.isDark;
  userMenuOpen = signal(false);

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

  toggleUserMenu() {
    this.userMenuOpen.update((v) => !v);
  }

  logout() {
    this.authService.logout();
  }
}
