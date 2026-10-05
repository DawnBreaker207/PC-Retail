import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { POLICY_CONTENT } from '@core/mocks/policy-content.mock';

@Component({
  selector: 'app-policy-page',
  imports: [RouterLink, NzBreadCrumbModule],
  templateUrl: './policy-page.component.html',
})
export class PolicyPageComponent {
  private route = inject(ActivatedRoute);

  slug = signal('');
  content = computed(() => POLICY_CONTENT[this.slug()]);

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.slug.set(params.get('slug') || '');
    });
  }
}
