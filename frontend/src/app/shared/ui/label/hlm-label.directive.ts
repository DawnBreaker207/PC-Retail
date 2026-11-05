import { computed, Directive, input } from '@angular/core';
import { cva } from 'class-variance-authority';
import { hlm } from '../core/hlm';

export const labelVariants = cva(
  'text-sm font-medium leading-none text-foreground peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
);

@Directive({
  selector: '[hlmLabel]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmLabelDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(labelVariants(), this.userClass())
  );
}
