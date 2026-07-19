import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

@Component({
  selector: 'app-page-header',
  imports: [RouterLink, NzBreadCrumbModule],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.css',
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input('');
  breadcrumbs = input<{ label: string; url?: string }[]>([]);
}
