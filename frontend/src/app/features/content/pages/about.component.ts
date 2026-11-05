import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight } from 'lucide-angular';

@Component({
  selector: 'app-about',
  imports: [RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './about.component.html',
})
export class AboutComponent {
  readonly ChevronRight = ChevronRight;
}
