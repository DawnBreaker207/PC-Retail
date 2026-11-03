import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';

@Component({
  selector: 'app-page-header',
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.css',
})
export class PageHeaderComponent {
  readonly ChevronRight = ChevronRight;

  title = input.required<string>();
  subtitle = input('');
  breadcrumbs = input<{ label: string; url?: string }[]>([]);
}
