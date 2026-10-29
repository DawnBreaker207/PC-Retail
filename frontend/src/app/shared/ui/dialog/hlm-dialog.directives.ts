import { computed, Directive, input } from '@angular/core';
import { hlm } from '../core/hlm';

@Directive({
  selector: '[hlmDialogOverlay]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogOverlayDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('fixed inset-0 z-50 bg-black/80 backdrop-blur-sm animate-in fade-in-0 duration-200', this.userClass())
  );
}

@Directive({
  selector: '[hlmDialogContent]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogContentDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(
      'fixed left-[50%] top-[50%] z-50 grid w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg duration-200 animate-in fade-in-0 zoom-in-95 sm:rounded-lg',
      this.userClass()
    )
  );
}

@Directive({
  selector: '[hlmDialogHeader]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogHeaderDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex flex-col space-y-1.5 text-center sm:text-left', this.userClass())
  );
}

@Directive({
  selector: '[hlmDialogFooter]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogFooterDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2', this.userClass())
  );
}

@Directive({
  selector: '[hlmDialogTitle]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogTitleDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-lg font-semibold leading-none tracking-tight', this.userClass())
  );
}

@Directive({
  selector: '[hlmDialogDescription]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmDialogDescriptionDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-sm text-muted-foreground', this.userClass())
  );
}
