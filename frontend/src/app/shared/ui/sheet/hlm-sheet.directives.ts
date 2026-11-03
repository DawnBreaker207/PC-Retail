import { computed, Directive, input } from '@angular/core';
import { cva, type VariantProps } from 'class-variance-authority';
import { hlm } from '../core/hlm';

@Directive({
  selector: '[hlmSheetOverlay]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetOverlayDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(
      'fixed inset-0 z-50 bg-black/60 backdrop-blur-xs transition-opacity duration-300 animate-in fade-in-0',
      this.userClass()
    )
  );
}

export const sheetVariants = cva(
  'fixed z-50 gap-4 bg-surface dark:bg-surface-dark p-6 shadow-2xl transition ease-in-out duration-300 animate-in border-border/80 dark:border-border-dark',
  {
    variants: {
      side: {
        top: 'inset-x-0 top-0 border-b slide-in-from-top',
        bottom: 'inset-x-0 bottom-0 border-t slide-in-from-bottom',
        left: 'inset-y-0 left-0 h-full w-3/4 border-r slide-in-from-left sm:max-w-md',
        right: 'inset-y-0 right-0 h-full w-full border-l slide-in-from-right sm:max-w-md md:max-w-lg',
      },
    },
    defaultVariants: {
      side: 'right',
    },
  }
);

export type SheetVariants = VariantProps<typeof sheetVariants>;

@Directive({
  selector: '[hlmSheetContent]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetContentDirective {
  readonly userClass = input<string>('', { alias: 'class' });
  readonly side = input<SheetVariants['side']>('right');

  protected readonly _computedClass = computed(() =>
    hlm(sheetVariants({ side: this.side() }), this.userClass())
  );
}

@Directive({
  selector: '[hlmSheetHeader]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetHeaderDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex flex-col space-y-2 text-left pb-4 border-b border-border/60 dark:border-border-dark/60', this.userClass())
  );
}

@Directive({
  selector: '[hlmSheetTitle]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetTitleDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-base font-bold text-ink dark:text-ink-dark', this.userClass())
  );
}

@Directive({
  selector: '[hlmSheetDescription]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetDescriptionDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-xs text-muted dark:text-muted-dark', this.userClass())
  );
}

@Directive({
  selector: '[hlmSheetFooter]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSheetFooterDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2 pt-4 border-t border-border/60 dark:border-border-dark/60 mt-auto', this.userClass())
  );
}
