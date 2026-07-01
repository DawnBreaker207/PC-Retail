import { Component, input } from '@angular/core';
import { NzSpinModule } from 'ng-zorro-antd/spin';

@Component({
  selector: 'app-loading-spinner',
  imports: [NzSpinModule],
  template: `
    @if (fullPage()) {
      <div class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/20">
        <nz-spin nzSize="large"></nz-spin>
      </div>
    } @else {
      <div class="flex justify-center py-10">
        <nz-spin nzSize="large"></nz-spin>
      </div>
    }
  `,
})
export class LoadingSpinnerComponent {
  fullPage = input(false);
  size = input<'small' | 'default' | 'large'>('large');
}
