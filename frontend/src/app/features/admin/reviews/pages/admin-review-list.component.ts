import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzRateModule } from 'ng-zorro-antd/rate';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Review } from '@core/models/common.model';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'HIDDEN', label: 'Đã ẩn' },
];

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'gold', APPROVED: 'success', HIDDEN: 'default',
};

@Component({
  selector: 'app-admin-review-list',
  imports: [
    DatePipe, FormsModule, NzTableModule, NzButtonModule, NzIconModule, NzSpinModule,
    NzTagModule, NzRateModule, NzSelectModule,
  ],
  templateUrl: './admin-review-list.component.html',
})
export class AdminReviewListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  reviews = signal<Review[]>([]);
  loading = signal(false);
  statusFilter = signal('');

  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;
  protected readonly STATUS_COLOR = STATUS_COLOR;

  constructor() {
    this.loadData();
  }

  onStatusChange(status: string) {
    this.statusFilter.set(status);
    this.loadData();
  }

  getStatusLabel(status: string | undefined): string {
    return STATUS_OPTIONS.find((o) => o.value === status)?.label || 'Chờ duyệt';
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams();
    if (this.statusFilter()) params = params.set('status', this.statusFilter());
    this.http.get<{ content: Review[] }>(ApiEndpoints.admin.reviews, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.reviews.set(res.content);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  moderate(review: Review, status: 'APPROVED' | 'HIDDEN') {
    this.http.post(ApiEndpoints.admin.reviewModerate(review.id), { status })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success(status === 'APPROVED' ? 'Đã duyệt đánh giá' : 'Đã ẩn đánh giá');
          this.loadData();
        },
        error: () => this.message.error('Thao tác thất bại'),
      });
  }
}
