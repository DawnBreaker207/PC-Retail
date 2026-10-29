import { computed, Directive, input } from '@angular/core';
import { hlm } from '../core/hlm';

@Directive({
  selector: '[hlmCard]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('rounded-xl border bg-card text-card-foreground shadow', this.userClass())
  );
}

@Directive({
  selector: '[hlmCardHeader]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardHeaderDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex flex-col space-y-1.5 p-6', this.userClass())
  );
}

@Directive({
  selector: '[hlmCardTitle]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardTitleDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('font-semibold leading-none tracking-tight', this.userClass())
  );
}

@Directive({
  selector: '[hlmCardDescription]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardDescriptionDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('text-sm text-muted-foreground', this.userClass())
  );
}

@Directive({
  selector: '[hlmCardContent]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardContentDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('p-6 pt-0', this.userClass())
  );
}

@Directive({
  selector: '[hlmCardFooter]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmCardFooterDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('flex items-center p-6 pt-0', this.userClass())
  );
}
