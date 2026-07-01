import { Component, input, output } from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartItem, ShippingAddress } from '@core/models/common.model';

@Component({
  selector: 'app-order-review',
  imports: [NzButtonModule, NzRadioModule, CurrencyFormatPipe],
  templateUrl: './order-review.component.html',
})
export class OrderReviewComponent {
  shippingAddress = input<ShippingAddress | null>(null);
  items = input<CartItem[]>([]);
  total = input(0);
  isPlacing = input(false);

  back = output();
  placeOrder = output();
}
