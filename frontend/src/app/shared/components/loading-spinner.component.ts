import { Component, input } from '@angular/core';
import { LucideAngularModule, Loader2 } from 'lucide-angular';

@Component({
  selector: 'app-loading-spinner',
  imports: [LucideAngularModule],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.css',
})
export class LoadingSpinnerComponent {
  readonly Loader2 = Loader2;

  fullPage = input(false);
  size = input<number | 'small' | 'default' | 'large'>('large');

  get pixelSize(): number {
    const s = this.size();
    if (typeof s === 'number') return s;
    if (s === 'small') return 20;
    if (s === 'default') return 28;
    return 36;
  }
}
