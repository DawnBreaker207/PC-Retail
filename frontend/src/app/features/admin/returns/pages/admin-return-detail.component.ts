import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AdminReturnRequest } from '@core/models/common.model';
import { AuthService } from '@core/auth/auth.service';
import { RejectReasonModalComponent } from '../components/reject-reason-modal.component';

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'gold', REQUESTED: 'gold', APPROVED: 'success', REJECTED: 'error', DISPUTED: 'processing',
};

const TYPE_LABEL: Record<string, string> = {
  DOA: 'Đổi mới (DOA)', WARRANTY: 'Bảo hành',
};

@Component({
  selector: 'app-admin-return-detail',
  imports: [DatePipe, RouterLink, NzTagModule, NzButtonModule, NzIconModule, NzSpinModule, NzToolTipModule, NzModalModule],
  templateUrl: './admin-return-detail.component.html',
})
export class AdminReturnDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);
  private authService = inject(AuthService);

  request = signal<AdminReturnRequest | null>(null);
  loading = signal(true);
  processing = signal(false);

  protected readonly STATUS_COLOR = STATUS_COLOR;
  protected readonly TYPE_LABEL = TYPE_LABEL;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData(id);
  }

  private loadData(id: number) {
    this.loading.set(true);
    this.http.get<AdminReturnRequest>(ApiEndpoints.admin.returnDetail(id))
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
    this.http.put<AdminReturnRequest>(ApiEndpoints.admin.returnApprove(r.id), { amount: 0 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.message.success('Đã duyệt yêu cầu trả hàng');
        },
        error: () => {
          this.processing.set(false);
          this.message.error('Duyệt yêu cầu thất bại');
        },
      });
  }

  openReject() {
    const r = this.request();
    if (!r) return;
    const modalRef = this.modal.create({
      nzTitle: 'Từ chối yêu cầu trả hàng',
      nzContent: RejectReasonModalComponent,
      nzFooter: null,
      nzWidth: 480,
    });
    modalRef.afterClose.subscribe((reason: string | undefined) => {
      if (reason) this.reject(r.id, reason);
    });
  }

  private reject(id: number, reason: string) {
    this.processing.set(true);
    this.http.put<AdminReturnRequest>(ApiEndpoints.admin.returnReject(id), { reason })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.message.success('Đã từ chối yêu cầu trả hàng');
        },
        error: () => {
          this.processing.set(false);
          this.message.error('Từ chối yêu cầu thất bại');
        },
      });
  }

  resolveDispute(overturn: boolean) {
    const r = this.request();
    if (!r) return;
    this.processing.set(true);
    this.http.put<AdminReturnRequest>(ApiEndpoints.admin.returnResolve(r.id), { overturn, note: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.request.set(updated);
          this.processing.set(false);
          this.message.success(overturn ? 'Đã chấp nhận khiếu nại' : 'Đã giữ nguyên quyết định');
        },
        error: () => {
          this.processing.set(false);
          this.message.error('Xử lý khiếu nại thất bại');
        },
      });
  }
}
