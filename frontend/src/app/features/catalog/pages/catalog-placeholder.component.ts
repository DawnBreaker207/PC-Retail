import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Hammer } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';

@Component({
  selector: 'app-catalog-placeholder',
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './catalog-placeholder.component.html',
  styleUrl: './catalog-placeholder.component.css',
})
export class CatalogPlaceholderComponent {
  readonly Hammer = Hammer;
}

