import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AdminReturnRequest } from '@core/models/common.model';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả' },
  { value: 'REQUESTED', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Đã từ chối' },
  { value: 'DISPUTED', label: 'Khiếu nại' },
  { value: 'REFUNDED', label: 'Đã hoàn tiền' },
];

const STATUS_COLOR: Record<string, string> = {
  REQUESTED: 'gold', APPROVED: 'success', REJECTED: 'error', DISPUTED: 'orange', REFUNDED: 'blue',
};

const TYPE_LABEL: Record<string, string> = {
  DOA: 'Đổi mới (DOA)', WARRANTY: 'Bảo hành',
};

@Component({
  selector: 'app-admin-return-list',
  imports: [
    DatePipe, RouterLink, FormsModule,
    NzTableModule, NzIconModule, NzSpinModule, NzTagModule, NzSelectModule,
  ],
  templateUrl: './admin-return-list.component.html',
})
export class AdminReturnListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  returns = signal<AdminReturnRequest[]>([]);
  loading = signal(false);
  statusFilter = signal('');

  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;
  protected readonly STATUS_COLOR = STATUS_COLOR;
  protected readonly TYPE_LABEL = TYPE_LABEL;

  constructor() {
    this.loadData();
  }

  onStatusChange(status: string) {
    this.statusFilter.set(status);
    this.loadData();
  }

  getStatusLabel(status: string): string {
    return STATUS_OPTIONS.find((o) => o.value === status)?.label || status;
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams();
    if (this.statusFilter()) params = params.set('status', this.statusFilter());
    this.http.get<AdminReturnRequest[]>(ApiEndpoints.admin.returns, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.returns.set(Array.isArray(res) ? res : (res as unknown as { content: AdminReturnRequest[] }).content ?? []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
