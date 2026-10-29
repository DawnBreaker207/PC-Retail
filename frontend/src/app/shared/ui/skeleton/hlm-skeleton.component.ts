import { Component, computed, input } from '@angular/core';
import { hlm } from '../core/hlm';

@Component({
  selector: 'hlm-skeleton',
  standalone: true,
  template: ``,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmSkeletonComponent {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('block animate-pulse rounded-md bg-primary/10', this.userClass())
  );
}
