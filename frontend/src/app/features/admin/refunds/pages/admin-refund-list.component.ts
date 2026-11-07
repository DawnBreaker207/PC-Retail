import {
  Component,
  inject,
  DestroyRef,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  Banknote,
  Search,
  CheckCircle,
  AlertCircle,
  Loader2,
  ExternalLink,
  CreditCard,
  Building2,
  User,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
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
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Refund } from '@core/models/common.model';

@Component({
  selector: 'app-admin-refund-list',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTooltipDirective,
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
    CurrencyFormatPipe,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-refund-list.component.html',
})
export class AdminRefundListComponent {
  readonly BanknoteIcon = Banknote;
  readonly SearchIcon = Search;
  readonly CheckCircleIcon = CheckCircle;
  readonly AlertCircleIcon = AlertCircle;
  readonly LoaderIcon = Loader2;
  readonly ExternalLinkIcon = ExternalLink;
  readonly CreditCardIcon = CreditCard;
  readonly BuildingIcon = Building2;
  readonly UserIcon = User;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  refunds = signal<Refund[]>([]);
  loading = signal(false);
  processing = signal(false);
  searchKeyword = signal('');

  selectedRefundToConfirm = signal<Refund | null>(null);

  filteredRefunds = computed(() => {
    const list = this.refunds();
    const query = this.searchKeyword().toLowerCase().trim();
    if (!query) return list;

    return list.filter((r) => {
      const matchId = String(r.id).includes(query);
      const matchSub = String(r.subOrderId).includes(query);
      const matchHolder = (r.accountHolder ?? '').toLowerCase().includes(query);
      const matchBank = (r.bankName ?? '').toLowerCase().includes(query);
      const matchAcc = (r.bankAccount ?? '').toLowerCase().includes(query);
      return matchId || matchSub || matchHolder || matchBank || matchAcc;
    });
  });

  totalPendingAmount = computed(() => {
    return this.filteredRefunds().reduce((sum, r) => sum + (r.amount || 0), 0);
  });

  constructor() {
    this.loadPendingRefunds();
  }

  loadPendingRefunds() {
    this.loading.set(true);
    this.http
      .get<any>(ApiEndpoints.returns.pendingRefunds)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const data = Array.isArray(res) ? res : res?.data ?? [];
          this.refunds.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Failed to load pending refunds', err);
          this.toast.error('Không thể tải danh sách hoàn tiền chờ xử lý');
          this.loading.set(false);
        },
      });
  }

  openConfirmModal(refund: Refund) {
    this.selectedRefundToConfirm.set(refund);
  }

  closeConfirmModal() {
    this.selectedRefundToConfirm.set(null);
  }

  confirmPaid() {
    const ref = this.selectedRefundToConfirm();
    if (!ref) return;

    this.processing.set(true);
    this.http
      .post(ApiEndpoints.returns.confirmPaid(ref.id), {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(`Đã xác nhận hoàn tiền #${ref.id} thành công!`);
          this.processing.set(false);
          this.closeConfirmModal();
          this.loadPendingRefunds();
        },
        error: (err) => {
          console.error('Confirm paid error', err);
          this.toast.error(err?.error?.message || 'Xác nhận hoàn tiền thất bại');
          this.processing.set(false);
        },
      });
  }
}
