import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideAngularModule, CheckCircle2, Clock, XCircle, ArrowRight } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './payment-result.component.html',
})
export class PaymentResultComponent {
  private route = inject(ActivatedRoute);

  readonly CheckCircleIcon = CheckCircle2;
  readonly ClockIcon = Clock;
  readonly XCircleIcon = XCircle;
  readonly ArrowRightIcon = ArrowRight;

  isSuccess = toSignal(
    this.route.queryParams.pipe(
      map((params) => params['vnp_ResponseCode'] === '00'),
    ),
    { initialValue: false },
  );

  isPlaced = toSignal(
    this.route.queryParams.pipe(
      map((params) => params['guest'] === '1' && params['vnp_ResponseCode'] == null),
    ),
    { initialValue: false },
  );

  orderId = toSignal(
    this.route.queryParams.pipe(map((params) => params['orderId'])),
    { initialValue: '' },
  );
}
