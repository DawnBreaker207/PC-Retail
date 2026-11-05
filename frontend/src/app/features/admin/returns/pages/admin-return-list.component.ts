import {
  Component,
  inject,
  DestroyRef,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LucideAngularModule, Filter, X, Loader2, ExternalLink } from 'lucide-angular';
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
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AdminReturnRequest } from '@core/models/common.model';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'REQUESTED', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Đã từ chối' },
  { value: 'DISPUTED', label: 'Khiếu nại' },
  { value: 'REFUNDED', label: 'Đã hoàn tiền' },
];

const TYPE_LABEL: Record<string, string> = {
  DOA: 'Đổi mới (DOA)',
  WARRANTY: 'Bảo hành',
};

@Component({
  selector: 'app-admin-return-list',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    RouterLink,
    FormsModule,
    LucideAngularModule,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-return-list.component.html',
})
export class AdminReturnListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly Filter = Filter;
  readonly X = X;
  readonly Loader2 = Loader2;
  readonly ExternalLink = ExternalLink;

  returns = signal<AdminReturnRequest[]>([]);
  loading = signal(false);
  statusFilter = signal('');
  orderIdFilter = signal<number | null>(null);

  filteredReturns = computed(() => {
    const list = this.returns();
    const oid = this.orderIdFilter();
    if (oid) {
      return list.filter((r) => r.orderId === oid);
    }
    return list;
  });

  protected readonly STATUS_OPTIONS = STATUS_OPTIONS;
  protected readonly TYPE_LABEL = TYPE_LABEL;

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const oid = params.get('orderId');
      this.orderIdFilter.set(oid ? Number(oid) : null);
    });
    this.loadData();
  }

  clearOrderFilter() {
    this.orderIdFilter.set(null);
    this.router.navigate([], { queryParams: { orderId: null }, queryParamsHandling: 'merge' });
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
    this.http
      .get<AdminReturnRequest[]>(ApiEndpoints.admin.returns, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.returns.set(
            Array.isArray(res)
              ? res
              : ((res as unknown as { content: AdminReturnRequest[] }).content ?? []),
          );
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
