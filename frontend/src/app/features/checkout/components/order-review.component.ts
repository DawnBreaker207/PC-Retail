import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { LucideAngularModule, MapPin, Loader2, Store, Truck, Building2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { CartItem, ShippingAddress, TaxInfo, FulfillmentType } from '@core/models/common.model';

@Component({
  selector: 'app-order-review',
  standalone: true,
  imports: [LucideAngularModule, HlmButtonDirective, CurrencyFormatPipe],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './order-review.component.html',
})
export class OrderReviewComponent {
  shippingAddress = input<ShippingAddress | null>(null);
  fulfillmentType = input<FulfillmentType>('DELIVERY');
  taxInfo = input<TaxInfo | undefined>(undefined);
  invoiceRequested = input(false);
  items = input<CartItem[]>([]);
  total = input(0);
  isPlacing = input(false);

  back = output();
  placeOrder = output();

  readonly MapPinIcon = MapPin;
  readonly LoaderIcon = Loader2;
  readonly StoreIcon = Store;
  readonly TruckIcon = Truck;
  readonly BuildingIcon = Building2;
}
