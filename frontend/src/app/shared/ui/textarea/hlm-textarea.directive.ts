import { computed, Directive, input } from '@angular/core';
import { cva, type VariantProps } from 'class-variance-authority';
import { hlm } from '../core/hlm';

export const textareaVariants = cva(
  'flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 transition-colors resize-none',
  {
    variants: {
      size: {
        default: 'min-h-[80px]',
        sm: 'min-h-[60px] text-xs',
        lg: 'min-h-[120px]',
      },
    },
    defaultVariants: {
      size: 'default',
    },
  }
);

export type TextareaVariants = VariantProps<typeof textareaVariants>;

@Directive({
  selector: '[hlmTextarea]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTextareaDirective {
  readonly userClass = input<string>('', { alias: 'class' });
  readonly size = input<TextareaVariants['size']>('default');

  protected readonly _computedClass = computed(() =>
    hlm(textareaVariants({ size: this.size() }), this.userClass())
  );
}
