import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  LucideAngularModule,
  ChevronDown,
  ChevronRight,
  Search,
  ShieldAlert,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
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
import { AuditLogEntry } from '@core/models/common.model';

const OUTCOME_OPTIONS = [
  { value: '', label: 'Tất cả kết quả' },
  { value: 'SUCCESS', label: 'Thành công' },
  { value: 'FAILURE', label: 'Thất bại' },
];

const RISKY_ACTION_PATTERN = /REFUND|DELETE|UPDATE|GRANT|REJECT/;

interface DiffField {
  key: string;
  oldValue: string;
  newValue: string;
}

@Component({
  selector: 'app-audit-log-list',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    LucideAngularModule,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './audit-log-list.component.html',
})
export class AuditLogListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  readonly ChevronDown = ChevronDown;
  readonly ChevronRight = ChevronRight;
  readonly Search = Search;
  readonly ShieldAlert = ShieldAlert;
  readonly Loader2 = Loader2;

  logs = signal<AuditLogEntry[]>([]);
  loading = signal(false);
  actorSearch = signal('');
  outcomeFilter = signal('');
  expandedId = signal<number | null>(null);

  protected readonly OUTCOME_OPTIONS = OUTCOME_OPTIONS;

  constructor() {
    this.loadData();
  }

  onSearch() {
    this.loadData();
  }

  onOutcomeChange(outcome: string) {
    this.outcomeFilter.set(outcome);
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams();
    if (this.outcomeFilter()) params = params.set('outcome', this.outcomeFilter());
    this.http
      .get<{ content: AuditLogEntry[] }>(ApiEndpoints.admin.auditLogs, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const search = this.actorSearch().trim().toLowerCase();
          this.logs.set(
            search
              ? res.content.filter((l) => l.actorName.toLowerCase().includes(search))
              : (res.content ?? []),
          );
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  isRisky(action: string): boolean {
    return RISKY_ACTION_PATTERN.test(action);
  }

  toggleExpand(id: number) {
    this.expandedId.set(this.expandedId() === id ? null : id);
  }

  getDiff(entry: AuditLogEntry): DiffField[] {
    const keys = new Set([
      ...Object.keys(entry.oldValue || {}),
      ...Object.keys(entry.newValue || {}),
    ]);
    return Array.from(keys).map((key) => ({
      key,
      oldValue: entry.oldValue?.[key] !== undefined ? String(entry.oldValue[key]) : '—',
      newValue: entry.newValue?.[key] !== undefined ? String(entry.newValue[key]) : '—',
    }));
  }
}
