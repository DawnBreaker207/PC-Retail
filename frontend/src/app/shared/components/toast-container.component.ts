import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import {
  LucideAngularModule,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  Info,
  X,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { ToastService } from '@core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [LucideAngularModule, HlmButtonDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div
      class="fixed top-4 right-4 z-[9999] flex flex-col gap-2 max-w-sm w-full pointer-events-none p-2"
    >
      @for (t of toastService.toasts(); track t.id) {
        <div
          class="pointer-events-auto flex items-start gap-3 p-3.5 rounded-xl border shadow-lg backdrop-blur-md transition-all duration-300 animate-in fade-in slide-in-from-top-2"
          [class.bg-surface]="true"
          [class.dark:bg-surface-dark]="true"
          [class.border-emerald-500/30]="t.type === 'success'"
          [class.text-emerald-700]="t.type === 'success'"
          [class.dark:text-emerald-300]="t.type === 'success'"
          [class.border-destructive/30]="t.type === 'error'"
          [class.text-destructive]="t.type === 'error'"
          [class.border-amber-500/30]="t.type === 'warning'"
          [class.text-amber-700]="t.type === 'warning'"
          [class.dark:text-amber-300]="t.type === 'warning'"
          [class.border-primary/30]="t.type === 'info'"
          [class.text-primary]="t.type === 'info'"
        >
          @switch (t.type) {
            @case ('success') {
              <lucide-icon
                [img]="CheckCircleIcon"
                class="size-5 shrink-0 text-emerald-500 mt-0.5"
              />
            }
            @case ('error') {
              <lucide-icon
                [img]="AlertCircleIcon"
                class="size-5 shrink-0 text-destructive mt-0.5"
              />
            }
            @case ('warning') {
              <lucide-icon
                [img]="AlertTriangleIcon"
                class="size-5 shrink-0 text-amber-500 mt-0.5"
              />
            }
            @default {
              <lucide-icon [img]="InfoIcon" class="size-5 shrink-0 text-primary mt-0.5" />
            }
          }
          <div class="flex-1 text-xs font-semibold leading-relaxed text-foreground">
            {{ t.message }}
          </div>
          <button
            hlmBtn
            variant="ghost"
            size="icon"
            type="button"
            (click)="toastService.dismiss(t.id)"
            class="size-6 text-muted hover:text-foreground p-0 rounded-md"
            aria-label="Đóng"
          >
            <lucide-icon [img]="XIcon" class="size-3.5" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);

  readonly CheckCircleIcon = CheckCircle2;
  readonly AlertCircleIcon = AlertCircle;
  readonly AlertTriangleIcon = AlertTriangle;
  readonly InfoIcon = Info;
  readonly XIcon = X;
}
