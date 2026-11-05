import { computed, Directive, input } from '@angular/core';
import { cva, type VariantProps } from 'class-variance-authority';
import { hlm } from '../core/hlm';

export const selectVariants = cva(
  'w-full appearance-none rounded-md border border-input bg-background px-3 pr-8 text-sm font-medium text-foreground shadow-sm transition-colors hover:border-primary/50 focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary disabled:cursor-not-allowed disabled:opacity-50',
  {
    variants: {
      size: {
        default: 'h-10',
        sm: 'h-9 px-2.5 text-xs',
        lg: 'h-12 px-4 text-base',
      },
    },
    defaultVariants: {
      size: 'default',
    },
  }
);

export type SelectVariants = VariantProps<typeof selectVariants>;

@Directive({
  selector: '[hlmSelect]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSelectDirective {
  readonly userClass = input<string>('', { alias: 'class' });
  readonly size = input<SelectVariants['size']>('default');

  protected readonly _computedClass = computed(() =>
    hlm(selectVariants({ size: this.size() }), this.userClass())
  );
}
