import { Component, computed, input, ChangeDetectionStrategy } from '@angular/core';

import { hlm } from '../core/hlm';

@Component({
  selector: 'hlm-progress',
  standalone: true,
  imports: [],
  template: `
    <div
      role="progressbar"
      [attr.aria-valuemin]="0"
      [attr.aria-valuemax]="max()"
      [attr.aria-valuenow]="value()"
      [class]="containerClass()"
    >
      <div [class]="indicatorClass()" [style.width.%]="percentage()"></div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  host: {
    class: 'block w-full',
  },
})
export class HlmProgressComponent {
  readonly value = input<number>(0);
  readonly max = input<number>(100);
  readonly userClass = input<string>('', { alias: 'class' });
  readonly indicatorColor = input<string>('');

  readonly percentage = computed(() => {
    const val = Math.max(0, Math.min(this.value(), this.max()));
    return this.max() > 0 ? (val / this.max()) * 100 : 0;
  });

  protected readonly containerClass = computed(() =>
    hlm(
      'relative h-2 w-full overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800',
      this.userClass(),
    ),
  );

  protected readonly indicatorClass = computed(() => {
    const custom = this.indicatorColor();
    if (custom) {
      return hlm('h-full transition-all duration-300 rounded-full', custom);
    }
    const pct = this.percentage();
    const defaultColor = pct >= 90 ? 'bg-rose-500' : pct >= 50 ? 'bg-amber-500' : 'bg-primary';
    return hlm('h-full transition-all duration-300 rounded-full', defaultColor);
  });
}
