import {
  Component,
  inject,
  DestroyRef,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  ShieldCheck,
  Search,
  CheckCircle,
  AlertCircle,
  Loader2,
  Wrench,
  Clock,
  Phone,
  User,
  ArrowRight,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { WarrantyClaim, WarrantyClaimStatus, WarrantyItem } from '@core/models/common.model';

@Component({
  selector: 'app-admin-warranty-list',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTextareaDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    HlmTooltipDirective,
  ],
  templateUrl: './admin-warranty-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class AdminWarrantyListComponent {
  readonly ShieldCheckIcon = ShieldCheck;
  readonly SearchIcon = Search;
  readonly CheckCircleIcon = CheckCircle;
  readonly AlertCircleIcon = AlertCircle;
  readonly LoaderIcon = Loader2;
  readonly WrenchIcon = Wrench;
  readonly ClockIcon = Clock;
  readonly PhoneIcon = Phone;
  readonly UserIcon = User;
  readonly ArrowRightIcon = ArrowRight;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  searchSubOrderId = signal('');
  warrantyItems = signal<WarrantyItem[]>([]);
  claims = signal<WarrantyClaim[]>([]);
  loading = signal(false);
  processing = signal(false);

  // Status update modal
  selectedClaim = signal<WarrantyClaim | null>(null);
  newStatus = signal<WarrantyClaimStatus>('DIAGNOSING');
  diagnosisNote = signal('');
  resolution = signal('');

  readonly statusOptions: { value: WarrantyClaimStatus; label: string }[] = [
    { value: 'CLAIMED', label: 'Mới tiếp nhận yêu cầu' },
    { value: 'RECEIVED', label: 'Đã nhận linh kiện tại trung tâm' },
    { value: 'DIAGNOSING', label: 'Đang kiểm tra & chẩn đoán lỗi' },
    { value: 'REPAIRED', label: 'Đã sửa chữa xong' },
    { value: 'REPLACED', label: 'Đã đổi mới linh kiện' },
    { value: 'REJECTED', label: 'Từ chối bảo hành' },
    { value: 'RETURNED_TO_CUSTOMER', label: 'Đã bàn giao trả khách' },
  ];

  lookupWarrantyBySubOrder() {
    const subId = Number(this.searchSubOrderId().trim());
    if (!subId) {
      this.toast.error('Vui lòng nhập mã kiện hàng SubOrder ID hợp lệ');
      return;
    }

    this.loading.set(true);
    this.http
      .get<WarrantyItem[]>(ApiEndpoints.warranty.bySubOrder(subId))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.warrantyItems.set(items ?? []);
          this.loading.set(false);
          if (items && items.length > 0) {
            this.loadClaimsForItems(items);
          } else {
            this.claims.set([]);
            this.toast.info('Không có linh kiện bảo hành nào cho kiện hàng này');
          }
        },
        error: (err) => {
          this.loading.set(false);
          console.error('Lookup warranty failed:', err);
          this.toast.error(err?.error?.message || 'Không thể tra cứu thông tin bảo hành');
        },
      });
  }

  private loadClaimsForItems(items: WarrantyItem[]) {
    const allClaims: WarrantyClaim[] = [];
    items.forEach((item) => {
      this.http
        .get<WarrantyClaim[]>(ApiEndpoints.warranty.claims(item.id))
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (res) => {
            if (res) allClaims.push(...res);
            this.claims.set([...allClaims]);
          },
        });
    });
  }

  openUpdateModal(claim: WarrantyClaim) {
    this.selectedClaim.set(claim);
    this.newStatus.set(claim.status);
    this.diagnosisNote.set(claim.diagnosisNote || '');
    this.resolution.set(claim.resolution || '');
  }

  closeUpdateModal() {
    this.selectedClaim.set(null);
  }

  submitUpdateStatus() {
    const claim = this.selectedClaim();
    if (!claim) return;

    this.processing.set(true);
    let url = ApiEndpoints.warranty.staffUpdateClaimStatus(claim.id) +
      `?status=${this.newStatus()}`;
    if (this.diagnosisNote().trim()) {
      url += `&diagnosisNote=${encodeURIComponent(this.diagnosisNote().trim())}`;
    }
    if (this.resolution().trim()) {
      url += `&resolution=${encodeURIComponent(this.resolution().trim())}`;
    }

    this.http
      .patch(url, {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(`Cập nhật tiến độ bảo hành #${claim.id} thành công!`);
          this.processing.set(false);
          this.closeUpdateModal();
          this.lookupWarrantyBySubOrder();
        },
        error: (err) => {
          this.processing.set(false);
          this.toast.error(err?.error?.message || 'Cập nhật tiến độ thất bại');
        },
      });
  }
}
