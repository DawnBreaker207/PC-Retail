import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule, Wrench, Monitor, PackageOpen, ArrowRight } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ResponsePage, BuildTemplate } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-template-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './template-list.component.html',
  styleUrl: './template-list.component.css',
})
export class TemplateListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  readonly WrenchIcon = Wrench;
  readonly MonitorIcon = Monitor;
  readonly PackageOpenIcon = PackageOpen;
  readonly ArrowRightIcon = ArrowRight;

  templates = signal<BuildTemplate[]>([]);
  loading = signal(true);

  constructor() {
    this.loadTemplates();
  }

  private loadTemplates() {
    this.loading.set(true);
    this.http
      .get<ResponsePage<BuildTemplate>>(ApiEndpoints.builder.templates, {
        params: { page: '0', size: '12' },
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.templates.set(res.content || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  getTags(tags: string[] | undefined): string[] {
    return tags ?? [];
  }
}
