import { computed, Directive, input } from '@angular/core';
import { cva, type VariantProps } from 'class-variance-authority';
import { hlm } from '../core/hlm';

export const separatorVariants = cva('shrink-0 bg-border/70 dark:bg-border-dark/70', {
  variants: {
    orientation: {
      horizontal: 'h-px w-full',
      vertical: 'h-full w-px',
    },
  },
  defaultVariants: {
    orientation: 'horizontal',
  },
});

export type SeparatorVariants = VariantProps<typeof separatorVariants>;

@Directive({
  selector: '[hlmSeparator]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
    'role': 'separator',
  },
})
export class HlmSeparatorDirective {
  readonly userClass = input<string>('', { alias: 'class' });
  readonly orientation = input<SeparatorVariants['orientation']>('horizontal');

  protected readonly _computedClass = computed(() =>
    hlm(separatorVariants({ orientation: this.orientation() }), this.userClass())
  );
}
