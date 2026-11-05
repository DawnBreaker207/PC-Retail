import { Component, computed, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LucideAngularModule } from 'lucide-angular';
import { POLICY_CONTENT } from '@core/mocks/policy-content.mock';
import { HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective } from '@shared/ui/breadcrumb';

@Component({
  selector: 'app-policy-page',
  imports: [RouterLink, LucideAngularModule, HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './policy-page.component.html',
})
export class PolicyPageComponent {
  readonly ChevronRight = ChevronRight;
  private route = inject(ActivatedRoute);

  slug = signal('');
  content = computed(() => POLICY_CONTENT[this.slug()]);

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.slug.set(params.get('slug') || '');
    });
  }
}
