import { Component, computed, effect, inject, signal, HostListener, ElementRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  Phone,
  Menu,
  Search,
  Heart,
  ShoppingCart,
  Sun,
  Moon,
  LayoutDashboard,
  User,
  FileText,
  LogOut,
  X,
  Wrench,
  MapPin,
  Bell,
  ArrowRight,
  TrendingUp,
} from 'lucide-angular';
import { ThemeService } from '@core/services/theme.service';
import { AuthService } from '@core/auth/auth.service';
import { CartStore } from '@features/cart/store/cart.store';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';
import { MockDataService } from '@core/services/mock-data.service';
import { NotificationBellComponent } from '@shared/components/notification-bell.component';
import { NotificationService } from '@core/services/notification.service';
import { RealtimeService } from '@core/services/realtime.service';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { Sku } from '@core/models/common.model';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    LucideAngularModule,
    NotificationBellComponent,
    CurrencyFormatPipe,
    FallbackImageDirective,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  readonly Phone = Phone;
  readonly Menu = Menu;
  readonly Search = Search;
  readonly Heart = Heart;
  readonly ShoppingCart = ShoppingCart;
  readonly Sun = Sun;
  readonly Moon = Moon;
  readonly LayoutDashboard = LayoutDashboard;
  readonly User = User;
  readonly FileText = FileText;
  readonly LogOut = LogOut;
  readonly X = X;
  readonly Wrench = Wrench;
  readonly MapPin = MapPin;
  readonly Bell = Bell;
  readonly ArrowRight = ArrowRight;
  readonly TrendingUp = TrendingUp;

  private router = inject(Router);
  private themeService = inject(ThemeService);
  private authService = inject(AuthService);
  private cartStore = inject(CartStore);
  private wishlistStore = inject(WishlistStore);
  private mock = inject(MockDataService);
  private notif = inject(NotificationService);
  private realtime = inject(RealtimeService);
  private el = inject(ElementRef);

  private allProducts: Sku[] = [];

  searchQuery = signal('');
  searchFocused = signal(false);

  popularSearches = [
    'RTX 4060',
    'Core i5 13400F',
    'PC Gaming DAWN',
    'Màn hình 144Hz',
    'RAM DDR5 32GB',
    'SSD NVMe 1TB',
  ];

  searchResults = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q || q.length < 2) return [];
    return this.allProducts
      .filter((p) => p.name.toLowerCase().includes(q) || (p.brandName && p.brandName.toLowerCase().includes(q)))
      .slice(0, 5);
  });

  constructor() {
    this.allProducts = this.mock.generateProducts();
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
  userMenuOpen = signal(false);

  toggleTheme() {
    this.themeService.toggle();
  }

  toggleUserMenu() {
    this.userMenuOpen.update((v) => !v);
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

  onSearchInput(val: string) {
    this.searchQuery.set(val);
  }

  onSearchFocus() {
    this.searchFocused.set(true);
  }

  closeSearch() {
    this.searchFocused.set(false);
  }

  selectProduct(p: Sku) {
    this.closeSearch();
    this.searchQuery.set('');
    this.router.navigate(['/products', p.slug]);
  }

  applyPopularSearch(term: string) {
    this.closeSearch();
    this.searchQuery.set(term);
    this.search(term);
  }

  search(query: string) {
    this.closeSearch();
    const q = query.trim();
    if (q) {
      this.router.navigate(['/products'], { queryParams: { q } });
    }
  }

  logout() {
    this.userMenuOpen.set(false);
    this.authService.logout();
  }

  categories = this.mock.categories;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const userMenuTarget = target.closest('.user-menu-container');
    if (!userMenuTarget) {
      this.userMenuOpen.set(false);
    }
    const searchTarget = target.closest('.search-box-container');
    if (!searchTarget) {
      this.searchFocused.set(false);
    }
  }
}
