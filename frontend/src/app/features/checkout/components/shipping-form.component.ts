import { Component, inject, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { ShippingAddress } from '@core/models/common.model';

@Component({
  selector: 'app-shipping-form',
  imports: [
    ReactiveFormsModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
    NzRadioModule,
    NzButtonModule,
  ],
  templateUrl: './shipping-form.component.html',
})
export class ShippingFormComponent {
  private fb = inject(FormBuilder);
  shippingComplete = output<ShippingAddress>();

  cities = ['Hà Nội', 'Hồ Chí Minh', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ'];

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^(0[3|5|7|8|9])+([0-9]{8})$/)]],
    street: ['', Validators.required],
    district: ['', Validators.required],
    city: ['', Validators.required],
    fulfillmentType: ['DELIVERY' as const],
  });

  submit() {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.shippingComplete.emit({ fullName: raw.fullName, phone: raw.phone, street: raw.street, district: raw.district, city: raw.city });
  }
}
