import {
  Component,
  inject,
  DestroyRef,
  signal,
  model,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  ArrowLeft,
  Check,
  X,
  AlertTriangle,
  Loader2,
  Calendar,
  User,
  Layers,
  HelpCircle,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AdminReturnRequest } from '@core/models/common.model';
import { AuthService } from '@core/auth/auth.service';

const TYPE_LABEL: Record<string, string> = {
  DOA: 'Đổi mới (DOA)',
  WARRANTY: 'Bảo hành',
};

@Component({
  selector: 'app-admin-return-detail',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    RouterLink,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmTextareaDirective,
    HlmBadgeDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-return-detail.component.html',
})
export class AdminReturnDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);
  private authService = inject(AuthService);

  readonly ArrowLeft = ArrowLeft;
  readonly Check = Check;
  readonly X = X;
  readonly AlertTriangle = AlertTriangle;
  readonly Loader2 = Loader2;
  readonly Calendar = Calendar;
  readonly User = User;
  readonly Layers = Layers;
  readonly HelpCircle = HelpCircle;

  request = signal<AdminReturnRequest | null>(null);
  loading = signal(true);
  processing = signal(false);

  // Reject dialog
  isRejectDialogOpen = signal(false);
  rejectReason = model('');

  protected readonly TYPE_LABEL = TYPE_LABEL;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData(id);
  }

  private loadData(id: number) {
    this.loading.set(true);
    this.http
      .get<AdminReturnRequest>(ApiEndpoints.admin.returnDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (r) => {
          this.request.set(r);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  get isSelfCreated(): boolean {
    const r = this.request();
    const currentUserId = this.authService.currentUser()?.id;
    return !!r && !!currentUserId && r.createdByUserId === currentUserId;
  }

  approve() {
    const r = this.request();
    if (!r) return;
    this.processing.set(true);
    this.http
      .put<AdminReturnRequest>(ApiEndpoints.admin.returnApprove(r.id), { amount: 0 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.toast.success('Đã duyệt yêu cầu trả hàng thành công');
        },
        error: () => {
          this.processing.set(false);
          this.toast.error('Duyệt yêu cầu thất bại');
        },
      });
  }

  openRejectDialog() {
    this.rejectReason.set('');
    this.isRejectDialogOpen.set(true);
  }

  closeRejectDialog() {
    this.isRejectDialogOpen.set(false);
  }

  submitReject() {
    const r = this.request();
    if (!r || !this.rejectReason().trim()) return;

    this.processing.set(true);
    this.http
      .put<AdminReturnRequest>(ApiEndpoints.admin.returnReject(r.id), {
        reason: this.rejectReason().trim(),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.closeRejectDialog();
          this.toast.success('Đã từ chối yêu cầu trả hàng');
        },
        error: () => {
          this.processing.set(false);
          this.toast.error('Từ chối yêu cầu thất bại');
        },
      });
  }

  resolveDispute(overturn: boolean) {
    const r = this.request();
    if (!r) return;
    this.processing.set(true);
    this.http
      .put<AdminReturnRequest>(ApiEndpoints.admin.returnResolve(r.id), { overturn, note: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.toast.success(
            overturn ? 'Đã chấp nhận khiếu nại của khách hàng' : 'Đã giữ nguyên quyết định từ chối',
          );
        },
        error: () => {
          this.processing.set(false);
          this.toast.error('Xử lý khiếu nại thất bại');
        },
      });
  }
}
