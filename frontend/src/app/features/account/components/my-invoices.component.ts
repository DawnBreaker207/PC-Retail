import {
  Component,
  inject,
  signal,
  OnInit,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  ReceiptText,
  FileCheck2,
  Clock,
  Building2,
  User,
  AlertCircle,
  Loader2,
  RefreshCw,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
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
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { ResponsePage, TaxInvoice } from '@core/models/common.model';
import { ToastService } from '@core/services/toast.service';

@Component({
  selector: 'app-my-invoices',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    LucideAngularModule,
    CurrencyFormatPipe,
    HlmButtonDirective,
    HlmBadgeDirective,
    HlmTooltipDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
  ],
  templateUrl: './my-invoices.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class MyInvoicesComponent implements OnInit {
  readonly ReceiptTextIcon = ReceiptText;
  readonly FileCheck2Icon = FileCheck2;
  readonly ClockIcon = Clock;
  readonly Building2Icon = Building2;
  readonly UserIcon = User;
  readonly AlertCircleIcon = AlertCircle;
  readonly LoaderIcon = Loader2;
  readonly RefreshCwIcon = RefreshCw;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  invoices = signal<TaxInvoice[]>([]);
  loading = signal(false);
  page = signal(0);
  pageSize = signal(20);
  totalElements = signal(0);

  ngOnInit() {
    this.loadInvoices();
  }

  loadInvoices() {
    this.loading.set(true);
    const url = `${ApiEndpoints.tax.myInvoices}?page=${this.page()}&size=${this.pageSize()}`;

    this.http
      .get<ResponsePage<TaxInvoice>>(url)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          this.invoices.set(res.content ?? []);
          this.totalElements.set(res.pagination?.totalElements ?? res.content?.length ?? 0);
        },
        error: (err) => {
          this.loading.set(false);
          console.error('Load invoices failed:', err);
          this.toast.error('Không thể tải danh sách hóa đơn VAT');
        },
      });
  }
}
