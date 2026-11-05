import { computed, Directive, input } from '@angular/core';
import { cva, type VariantProps } from 'class-variance-authority';
import { hlm } from '../core/hlm';

export const tabsListVariants = cva(
  'flex items-center gap-1 overflow-x-auto border-b border-border/70 dark:border-border-dark/70 pb-px scrollbar-none',
  {
    variants: {},
    defaultVariants: {},
  }
);

export const tabsTriggerVariants = cva(
  'px-4 py-2 text-sm font-medium whitespace-nowrap border-b-2 transition-colors transition-border cursor-pointer',
  {
    variants: {
      state: {
        active: 'border-primary text-primary font-semibold',
        inactive: 'border-transparent text-muted dark:text-muted-dark hover:text-foreground',
      },
    },
    defaultVariants: {
      state: 'inactive',
    },
  }
);

export type TabsTriggerVariants = VariantProps<typeof tabsTriggerVariants>;

@Directive({
  selector: '[hlmTabsList]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTabsListDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(tabsListVariants(), this.userClass())
  );
}

@Directive({
  selector: '[hlmTabsTrigger]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTabsTriggerDirective {
  readonly userClass = input<string>('', { alias: 'class' });
  readonly state = input<TabsTriggerVariants['state']>('inactive');

  protected readonly _computedClass = computed(() =>
    hlm(tabsTriggerVariants({ state: this.state() }), this.userClass())
  );
}
