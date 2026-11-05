import { Component, computed, Directive, input, ChangeDetectionStrategy } from '@angular/core';
import { hlm } from '../core/hlm';

@Component({
  selector: 'hlm-avatar',
  standalone: true,
  template: `<ng-content />`,
  changeDetection: ChangeDetectionStrategy.Eager,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmAvatarComponent {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('relative flex size-10 shrink-0 overflow-hidden rounded-full', this.userClass()),
  );
}

@Directive({
  selector: '[hlmAvatarImage]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmAvatarImageDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('aspect-square size-full object-cover', this.userClass()),
  );
}

@Directive({
  selector: '[hlmAvatarFallback]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmAvatarFallbackDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(
      'flex size-full items-center justify-center rounded-full bg-muted font-medium text-xs',
      this.userClass(),
    ),
  );
}
