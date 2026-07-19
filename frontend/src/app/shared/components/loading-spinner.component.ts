import { Component, input } from '@angular/core';
import { NzSpinModule } from 'ng-zorro-antd/spin';

@Component({
  selector: 'app-loading-spinner',
  imports: [NzSpinModule],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.css',
})
export class LoadingSpinnerComponent {
  fullPage = input(false);
  size = input<'small' | 'default' | 'large'>('large');
}
