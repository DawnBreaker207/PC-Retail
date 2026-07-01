import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

@Component({
  selector: 'app-page-header',
  imports: [RouterLink, NzBreadCrumbModule],
  template: `
    @if (breadcrumbs().length > 0) {
      <nz-breadcrumb class="mb-2">
        @for (crumb of breadcrumbs(); track crumb.label) {
          <nz-breadcrumb-item>
            @if (crumb.url) {
              <a [routerLink]="crumb.url">{{ crumb.label }}</a>
            } @else {
              {{ crumb.label }}
            }
          </nz-breadcrumb-item>
        }
      </nz-breadcrumb>
    }
    <h1 class="text-2xl font-bold mb-1">{{ title() }}</h1>
    @if (subtitle()) {
      <p class="text-muted dark:text-muted-dark text-sm mb-6">{{ subtitle() }}</p>
    }
  `,
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input('');
  breadcrumbs = input<{ label: string; url?: string }[]>([]);
}
