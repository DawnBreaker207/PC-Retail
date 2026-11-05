import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LucideAngularModule, Star, Check, EyeOff, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Review } from '@core/models/common.model';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'HIDDEN', label: 'Đã ẩn' },
];

@Component({
  selector: 'app-admin-review-list',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-review-list.component.html',
})
export class AdminReviewListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly Star = Star;
  readonly Check = Check;
  readonly EyeOff = EyeOff;
  readonly Loader2 = Loader2;

  reviews = signal<Review[]>([]);
  loading = signal(false);
  statusFilter = signal('');

  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;

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
    this.http
      .get<{ content: Review[] }>(ApiEndpoints.admin.reviews, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.reviews.set(res.content ?? []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  moderate(review: Review, status: 'APPROVED' | 'HIDDEN') {
    this.http
      .post(ApiEndpoints.admin.reviewModerate(review.id), { status })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(status === 'APPROVED' ? 'Đã duyệt đánh giá' : 'Đã ẩn đánh giá');
          this.loadData();
        },
        error: () => this.toast.error('Thao tác thất bại'),
      });
  }
}
