import { computed, Directive, input } from '@angular/core';
import { hlm } from '../core/hlm';

@Directive({
  selector: '[hlmTable]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('w-full caption-bottom text-sm border-collapse', this.userClass())
  );
}

@Directive({
  selector: '[hlmTableHeader]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableHeaderDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('[&_tr]:border-b border-border/80 dark:border-border-dark', this.userClass())
  );
}

@Directive({
  selector: '[hlmTableBody]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableBodyDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('[&_tr:last-child]:border-0', this.userClass())
  );
}

@Directive({
  selector: '[hlmTableRow]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableRowDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(
      'border-b border-border/60 dark:border-border-dark/60 transition-colors hover:bg-slate-100/50 dark:hover:bg-slate-800/40 data-[state=selected]:bg-muted',
      this.userClass()
    )
  );
}

@Directive({
  selector: '[hlmTableHead]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableHeadDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm(
      'h-10 px-3 text-left align-middle font-semibold text-xs text-muted dark:text-muted-dark uppercase tracking-wider [&:has([role=checkbox])]:pr-0',
      this.userClass()
    )
  );
}

@Directive({
  selector: '[hlmTableCell]',
  standalone: true,
  host: {
    '[class]': '_computedClass()',
  },
})
export class HlmTableCellDirective {
  readonly userClass = input<string>('', { alias: 'class' });

  protected readonly _computedClass = computed(() =>
    hlm('p-3 align-middle text-ink dark:text-ink-dark [&:has([role=checkbox])]:pr-0', this.userClass())
  );
}
