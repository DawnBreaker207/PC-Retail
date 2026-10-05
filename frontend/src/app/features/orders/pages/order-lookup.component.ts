import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { OrderService } from '@core/services/order.service';
import { Order } from '@core/models/common.model';

@Component({
  selector: 'app-order-lookup',
  imports: [
    RouterLink, ReactiveFormsModule, NzBreadCrumbModule, NzFormModule,
    NzInputModule, NzButtonModule, NzTagModule, CurrencyFormatPipe, FallbackImageDirective,
  ],
  templateUrl: './order-lookup.component.html',
})
export class OrderLookupComponent {
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);

  isLoading = signal(false);
  searched = signal(false);
  order = signal<Order | null>(null);

  form = this.fb.nonNullable.group({
    orderId: ['', Validators.required],
    phone: ['', Validators.required],
  });

  submit() {
    if (this.form.invalid) return;
    const { orderId, phone } = this.form.getRawValue();
    const id = Number(orderId.replace(/\D/g, ''));
    if (!id) {
      this.searched.set(true);
      this.order.set(null);
      return;
    }
    this.isLoading.set(true);
    this.orderService.getOrderDetail(id).pipe(
      finalize(() => { this.isLoading.set(false); this.searched.set(true); }),
    ).subscribe({
      next: (o) => {
        const matches = o.shippingAddress?.phone?.replace(/\D/g, '') === phone.replace(/\D/g, '');
        this.order.set(matches ? o : null);
      },
      error: () => this.order.set(null),
    });
  }
}
