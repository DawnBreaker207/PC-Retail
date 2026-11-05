import { computed, Directive, input } from '@angular/core';
import { hlm } from '../core/hlm';

@Directive({
  selector: '[hlmBreadcrumb]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
    'aria-label': 'Breadcrumb',
  },
})
export class HlmBreadcrumbDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex items-center gap-1.5 text-xs text-muted dark:text-muted-dark', this.userClass())
  );
}

@Directive({
  selector: '[hlmBreadcrumbLink]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmBreadcrumbLinkDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('hover:text-primary transition-colors', this.userClass())
  );
}

@Directive({
  selector: '[hlmBreadcrumbCurrent]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
    'aria-current': 'page',
  },
})
export class HlmBreadcrumbCurrentDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-foreground font-medium', this.userClass())
  );
}
