import {
  Component,
  inject,
  signal,
  computed,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  ChevronRight,
  Star,
  Minus,
  Plus,
  ShoppingCart,
  Heart,
  Phone,
  MessageCircle,
  CheckCircle2,
  RotateCcw,
  Truck,
  ShieldCheck,
  MessageSquare,
  Gift,
  Sparkles,
  Zap,
  Check,
  MapPin,
  Building2,
  CreditCard,
  X,
} from 'lucide-angular';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { MockDataService } from '@core/services/mock-data.service';
import { SeoService } from '@core/services/seo.service';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Sku, Review } from '@core/models/common.model';
import { CartStore } from '@features/cart/store/cart.store';
import { WishlistStore } from '@features/wishlist/store/wishlist.store';
import { RatingService } from '@core/services/rating.service';
import { SkuRatingSummary } from '@core/models/common.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    LucideAngularModule,
    HlmBadgeDirective,
    HlmButtonDirective,
    CurrencyFormatPipe,
    FallbackImageDirective,
    ProductCardComponent,
    LoadingSpinnerComponent,
  ],
  templateUrl: './product-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './product-detail.component.css',
})
export class ProductDetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private mock = inject(MockDataService);
  private seo = inject(SeoService);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  cartStore = inject(CartStore);
  wishlist = inject(WishlistStore);
  private ratingService = inject(RatingService);
  ratingSummary = signal<SkuRatingSummary | null>(null);

  readonly ChevronRightIcon = ChevronRight;
  readonly StarIcon = Star;
  readonly MinusIcon = Minus;
  readonly PlusIcon = Plus;
  readonly CartIcon = ShoppingCart;
  readonly HeartIcon = Heart;
  readonly PhoneIcon = Phone;
  readonly MessageCircleIcon = MessageCircle;
  readonly CheckCircle2Icon = CheckCircle2;
  readonly RotateCcwIcon = RotateCcw;
  readonly TruckIcon = Truck;
  readonly ShieldCheckIcon = ShieldCheck;
  readonly MessageSquareIcon = MessageSquare;
  readonly GiftIcon = Gift;
  readonly SparklesIcon = Sparkles;
  readonly ZapIcon = Zap;
  readonly CheckIcon = Check;
  readonly MapPinIcon = MapPin;
  readonly Building2Icon = Building2;
  readonly CreditCardIcon = CreditCard;
  readonly XIcon = X;

  showShowroomList = signal(false);
  showInstallmentModal = signal(false);
  selectedDownPayment = signal(0);
  selectedMonths = signal(12);

  showrooms = [
    {
      city: 'Hà Nội',
      name: 'Showroom Đống Đa',
      address: '180 Thái Hà, P. Trung Liệt, Q. Đống Đa',
      phone: '024.7305.3456',
      stock: 'Còn hàng (3 máy)',
      inStock: true,
    },
    {
      city: 'Hà Nội',
      name: 'Showroom Hai Bà Trưng',
      address: '79 Trần Đại Nghĩa, P. Bách Khoa, Q. Hai Bà Trưng',
      phone: '024.7305.8899',
      stock: 'Còn hàng (2 máy)',
      inStock: true,
    },
    {
      city: 'TP. Hồ Chí Minh',
      name: 'Showroom Tân Bình',
      address: '78-80 Hoàng Hoa Thám, P.12, Q. Tân Bình',
      phone: '028.7108.1800',
      stock: 'Còn hàng (5 máy)',
      inStock: true,
    },
    {
      city: 'TP. Hồ Chí Minh',
      name: 'Showroom Quận 1',
      address: '90-92 Nguyễn Thị Minh Khai, P. Đa Kao, Q.1',
      phone: '028.7108.6688',
      stock: 'Còn hàng (2 máy)',
      inStock: true,
    },
    {
      city: 'TP. Hồ Chí Minh',
      name: 'Showroom Thủ Đức',
      address: '905 Kha Vạn Cân, P. Linh Chiểu, TP. Thủ Đức',
      phone: '028.7108.9922',
      stock: 'Liên hệ trước',
      inStock: false,
    },
  ];

  installmentMonthly = computed(() => {
    const p = this.product();
    if (!p) return 0;
    const downAmount = (p.price * this.selectedDownPayment()) / 100;
    const remaining = p.price - downAmount;
    return Math.round(remaining / this.selectedMonths());
  });

  downPaymentAmount = computed(() => {
    const p = this.product();
    if (!p) return 0;
    return Math.round((p.price * this.selectedDownPayment()) / 100);
  });

  product = signal<Sku | null>(null);
  reviews: Review[] = [];
  relatedProducts: Sku[] = [];
  loading = true;
  quantity = 1;
  selectedIndex = 0;
  selectedImage = signal('');
  activeTab = signal<'desc' | 'specs' | 'reviews'>('desc');

  discountPercent = computed(() => {
    const p = this.product();
    if (!p?.originalPrice || p.originalPrice <= p.price) return 0;
    return Math.round((1 - p.price / p.originalPrice) * 100);
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const slug = params.get('slug');
      if (slug) this.loadProduct(slug);
    });
  }

  private loadProduct(slug: string) {
    this.loading = true;
    this.mock
      .getProduct(slug)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => {
          if (p) {
            this.product.set(p);
            this.selectedImage.set(p.imageUrls[0] || '');
            this.seo.setProduct(p);
            this.trackView(p);
            this.loadReviews();
            this.loadRelated(slug);
          }
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }

  private loadReviews() {
    const skuId = this.product()?.id;
    if (!skuId) return;

    this.ratingService
      .getSkuRatings(skuId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        if (r && r.length > 0) {
          this.reviews = r.map((item) => ({
            id: item.id,
            userId: item.userId,
            fullName: `Khách hàng #${item.userId}`,
            rating: item.score,
            comment: item.comment,
            images: item.imageUrls,
            createdAt: item.createdAt,
            status: 'APPROVED',
            skuId: item.skuId,
          }));
        } else {
          this.mock
            .getReviews()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((mockR) => (this.reviews = mockR));
        }
      });

    this.ratingService
      .getSkuSummary(skuId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((summary) => this.ratingSummary.set(summary));
  }

  private trackView(p: Sku) {
    this.http
      .post(ApiEndpoints.catalog.skuView(p.id), {
        componentCategory: p.categoryName ?? '',
        price: p.price,
        specs: p.specs ?? {},
      })
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  private loadRelated(slug: string) {
    const id = this.product()?.id;
    if (id == null) {
      this.loadRelatedMock(slug);
      return;
    }
    this.http
      .get<Sku[]>(`${ApiEndpoints.aiSimilar}/${id}`)
      .pipe(
        catchError(() => this.mock.getRelatedProducts(slug)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((r) => (this.relatedProducts = Array.isArray(r) ? r : []));
  }

  private loadRelatedMock(slug: string) {
    this.mock
      .getRelatedProducts(slug)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => (this.relatedProducts = r));
  }

  selectImage(index: number) {
    this.selectedIndex = index;
    const p = this.product();
    if (p?.imageUrls[index]) this.selectedImage.set(p.imageUrls[index]);
  }

  decrementQty() {
    if (this.quantity > 1) this.quantity--;
  }

  incrementQty() {
    const p = this.product();
    if (p?.stockAvailable && this.quantity < p.stockAvailable) this.quantity++;
  }

  addToCart(p: Sku) {
    this.cartStore.addItem(p.id, this.quantity);
  }

  buyNow(p: Sku) {
    this.cartStore.addItem(p.id, this.quantity);
    this.router.navigate(['/checkout']);
  }

  specEntries = computed<{ key: string; value: string }[]>(() => {
    const p = this.product();
    if (p?.specChips && p.specChips.length > 0) {
      return p.specChips.map((chip) => ({ key: chip.label, value: chip.value }));
    }
    if (p?.specs) {
      return Object.entries(p.specs).map(([key, value]) => ({ key, value }));
    }
    return [];
  });
}
