import {
  Component,
  inject,
  DestroyRef,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  Truck,
  Package,
  Phone,
  MapPin,
  CheckCircle,
  XCircle,
  AlertTriangle,
  RefreshCw,
  Calendar,
  DollarSign,
  Loader2,
  Navigation,
  ArrowRight,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ToastService } from '@core/services/toast.service';
import { AuthService } from '@core/auth/auth.service';
import { ShipperService } from '../services/shipper.service';
import { SubOrder } from '@core/models/common.model';

@Component({
  selector: 'app-shipper-orders',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    LucideAngularModule,
    HlmButtonDirective,
    HlmTooltipDirective,
    HlmTextareaDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    CurrencyFormatPipe,
  ],
  templateUrl: './shipper-orders.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ShipperOrdersComponent {
  readonly TruckIcon = Truck;
  readonly PackageIcon = Package;
  readonly PhoneIcon = Phone;
  readonly MapPinIcon = MapPin;
  readonly CheckCircleIcon = CheckCircle;
  readonly XCircleIcon = XCircle;
  readonly AlertTriangleIcon = AlertTriangle;
  readonly RefreshCwIcon = RefreshCw;
  readonly CalendarIcon = Calendar;
  readonly DollarSignIcon = DollarSign;
  readonly LoaderIcon = Loader2;
  readonly NavigationIcon = Navigation;
  readonly ArrowRightIcon = ArrowRight;

  private shipperService = inject(ShipperService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  currentUser = this.auth.currentUser;
  orders = signal<SubOrder[]>([]);
  loading = signal(false);
  activeTab = signal<string>('ALL');
  processingSubOrderId = signal<number | null>(null);

  // Delivery Success Modal
  deliverModal = signal<{
    open: boolean;
    subOrder: SubOrder;
    codCollected: boolean;
  } | null>(null);

  // Delivery Failure Modal
  failModal = signal<{
    open: boolean;
    subOrder: SubOrder;
    note: string;
  } | null>(null);

  readonly failureQuickNotes = [
    'Khách không nghe máy (đã gọi 3 lần)',
    'Khách hẹn giao lại vào hôm sau',
    'Địa chỉ không đúng hoặc không tìm thấy',
    'Khách từ chối nhận hàng',
    'Hàng bị móp méo / khách không đồng ý nhận',
  ];

  filteredOrders = computed(() => {
    const list = this.orders();
    const tab = this.activeTab();
    if (tab === 'ALL') return list;
    return list.filter((o) => o.status === tab);
  });

  countReady = computed(() => this.orders().filter((o) => o.status === 'READY_TO_SHIP').length);
  countDelivering = computed(() => this.orders().filter((o) => o.status === 'OUT_FOR_DELIVERY').length);
  countDelivered = computed(() => this.orders().filter((o) => o.status === 'DELIVERED').length);
  countFailed = computed(() => this.orders().filter((o) => o.status === 'DELIVERY_FAILED').length);

  constructor() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading.set(true);
    this.shipperService
      .getMyOrders(0, 50)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.orders.set(res?.content ?? []);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Failed to load shipper orders', err);
          this.toast.error('Không thể tải danh sách đơn giao');
          this.loading.set(false);
        },
      });
  }

  pickup(subOrder: SubOrder) {
    this.processingSubOrderId.set(subOrder.id);
    this.shipperService
      .pickup(subOrder.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(`Đã nhận kiện hàng #SO-${subOrder.id} để đi giao!`);
          this.processingSubOrderId.set(null);
          this.loadOrders();
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Thao tác nhận hàng thất bại');
          this.processingSubOrderId.set(null);
        },
      });
  }

  openDeliverModal(subOrder: SubOrder) {
    this.deliverModal.set({
      open: true,
      subOrder,
      codCollected: true,
    });
  }

  closeDeliverModal() {
    this.deliverModal.set(null);
  }

  confirmDelivered() {
    const data = this.deliverModal();
    if (!data) return;

    this.processingSubOrderId.set(data.subOrder.id);
    this.shipperService
      .delivered(data.subOrder.id, data.codCollected)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(`Đã giao thành công kiện #SO-${data.subOrder.id}!`);
          this.processingSubOrderId.set(null);
          this.closeDeliverModal();
          this.loadOrders();
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Cập nhật giao thành công thất bại');
          this.processingSubOrderId.set(null);
        },
      });
  }

  openFailModal(subOrder: SubOrder) {
    this.failModal.set({
      open: true,
      subOrder,
      note: '',
    });
  }

  selectQuickNote(note: string) {
    const cur = this.failModal();
    if (cur) {
      this.failModal.set({ ...cur, note });
    }
  }

  closeFailModal() {
    this.failModal.set(null);
  }

  confirmFailed() {
    const data = this.failModal();
    if (!data || !data.note.trim()) {
      this.toast.error('Vui lòng nhập lý do giao hàng thất bại');
      return;
    }

    this.processingSubOrderId.set(data.subOrder.id);
    this.shipperService
      .failed(data.subOrder.id, data.note.trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.warning(`Đã ghi nhận giao thất bại kiện #SO-${data.subOrder.id}`);
          this.processingSubOrderId.set(null);
          this.closeFailModal();
          this.loadOrders();
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Cập nhật thất bại');
          this.processingSubOrderId.set(null);
        },
      });
  }
}
