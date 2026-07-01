import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-empty-state',
  imports: [RouterLink, NzEmptyModule, NzButtonModule],
  template: `
    <nz-empty
      nzNotFoundImage="simple"
      [nzNotFoundContent]="title()"
    >
      <div nz-not-found-content>
        <p class="text-muted dark:text-muted-dark">{{ description() }}</p>
      </div>
    </nz-empty>
    @if (actionLabel() && actionRoute()) {
      <div class="text-center mt-4">
        <button nz-button nzType="primary" [routerLink]="actionRoute()">
          {{ actionLabel() }}
        </button>
      </div>
    }
  `,
})
export class EmptyStateComponent {
  title = input('Không có dữ liệu');
  description = input('');
  actionLabel = input('');
  actionRoute = input('');
}
