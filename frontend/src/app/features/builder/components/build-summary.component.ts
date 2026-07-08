import { Component, input, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CartStore } from '@features/cart/store/cart.store';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { BuilderSlot, BuilderSelection, BuilderValidation } from '@core/models/common.model';

@Component({
  selector: 'app-build-summary',
  imports: [
    CommonModule, FallbackImageDirective, NzButtonModule, NzIconModule, NzAlertModule,
    NzDividerModule, CurrencyFormatPipe,
  ],
  templateUrl: './build-summary.component.html',
  styleUrl: './build-summary.component.css',
})
export class BuildSummaryComponent {
  private router = inject(Router);
  private message = inject(NzMessageService);
  private cartStore = inject(CartStore);

  selections = input<Map<string, BuilderSelection>>(new Map());
  slots = input<BuilderSlot[]>([]);
  validation = input<BuilderValidation | null>(null);
  canAddToCart = input(false);
  missingRequired = input(false);

  addToCart = output<void>();

  get selectedItems(): BuilderSelection[] {
    return Array.from(this.selections().values());
  }

  get totalPrice(): number {
    return this.selectedItems.reduce((sum, s) => sum + s.sku.price, 0);
  }

  get isValid(): boolean {
    return this.validation()?.valid ?? true;
  }

  get conflicts() {
    return this.validation()?.conflicts ?? [];
  }

  onAddToCart() {
    const items = this.selectedItems;
    if (items.length === 0) return;

    items.forEach(item => {
      this.cartStore.addItem(item.sku.id, 1);
    });
    this.message.success('Đã thêm cấu hình vào giỏ hàng!');
    this.router.navigate(['/cart']);
  }
}
