import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-empty-state',
  imports: [RouterLink, NzEmptyModule, NzButtonModule],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.css',
})
export class EmptyStateComponent {
  title = input('Không có dữ liệu');
  description = input('');
  actionLabel = input('');
  actionRoute = input('');
}
