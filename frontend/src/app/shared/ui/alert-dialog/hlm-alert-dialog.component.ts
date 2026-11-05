import {
  Component,
  input,
  output,
  ChangeDetectionStrategy,
} from '@angular/core';
import { HlmButtonDirective } from '../button';

@Component({
  selector: 'hlm-alert-dialog',
  standalone: true,
  imports: [HlmButtonDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (open()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-xs" (click)="onCancel()"></div>
        <div
          role="alertdialog"
          aria-modal="true"
          [attr.aria-labelledby]="'alert-title-' + _id"
          [attr.aria-describedby]="'alert-desc-' + _id"
          class="relative z-10 w-full max-w-sm bg-surface dark:bg-surface-dark rounded-2xl border border-border/80 dark:border-border-dark shadow-xl p-6 animate-in fade-in zoom-in-95 duration-200"
        >
          <h2 [id]="'alert-title-' + _id" class="text-base font-bold text-ink dark:text-ink-dark mb-1">
            {{ title() }}
          </h2>
          @if (description()) {
            <p [id]="'alert-desc-' + _id" class="text-sm text-muted dark:text-muted-dark mb-5">
              {{ description() }}
            </p>
          }
          <div class="flex justify-end gap-2 mt-5">
            <button
              hlmBtn
              variant="outline"
              size="sm"
              type="button"
              (click)="onCancel()"
            >
              {{ cancelLabel() }}
            </button>
            <button
              hlmBtn
              [attr.variant]="destructive() ? 'destructive' : 'default'"
              size="sm"
              type="button"
              (click)="onConfirm()"
            >
              {{ confirmLabel() }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class HlmAlertDialogComponent {
  readonly _id = Math.random().toString(36).slice(2, 8);

  open = input(false);
  title = input('Xác nhận');
  description = input('');
  confirmLabel = input('Xác nhận');
  cancelLabel = input('Hủy');
  destructive = input(false);

  confirmed = output<void>();
  cancelled = output<void>();

  onConfirm() {
    this.confirmed.emit();
  }

  onCancel() {
    this.cancelled.emit();
  }
}
