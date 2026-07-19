import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Location } from '@angular/common';
import { NzResultModule } from 'ng-zorro-antd/result';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-admin-placeholder',
  imports: [RouterLink, NzResultModule, NzButtonModule],
  templateUrl: './admin-placeholder.component.html',
  styleUrl: './admin-placeholder.component.css',
})
export class AdminPlaceholderComponent {
  private location = inject(Location);
  get title(): string {
    const segments = this.location.path().split('/').filter(Boolean);
    const last = segments[segments.length - 1];
    if (!last || last === 'admin') return 'Dashboard';
    return last.charAt(0).toUpperCase() + last.slice(1);
  }
}
