import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NzResultModule } from 'ng-zorro-antd/result';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  selector: 'app-payment-result',
  imports: [RouterLink, NzResultModule, NzButtonModule],
  templateUrl: './payment-result.component.html',
})
export class PaymentResultComponent {
  private route = inject(ActivatedRoute);

  isSuccess = toSignal(
    this.route.queryParams.pipe(
      map((params) => params['vnp_ResponseCode'] === '00'),
    ),
    { initialValue: false },
  );

  orderId = toSignal(
    this.route.queryParams.pipe(map((params) => params['orderId'])),
    { initialValue: '' },
  );
}
