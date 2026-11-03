import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Inbox } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';

@Component({
  selector: 'app-empty-state',
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.css',
})
export class EmptyStateComponent {
  readonly Inbox = Inbox;

  title = input('Không có dữ liệu');
  description = input('');
  actionLabel = input('');
  actionRoute = input('');
}

