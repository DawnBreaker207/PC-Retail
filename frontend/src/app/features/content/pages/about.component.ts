import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';
import { HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective } from '@shared/ui/breadcrumb';

@Component({
  selector: 'app-about',
  imports: [RouterLink, LucideAngularModule, HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './about.component.html',
})
export class AboutComponent {
  readonly ChevronRight = ChevronRight;
}
