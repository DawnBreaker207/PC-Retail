import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  Truck,
  RotateCcw,
  ShieldCheck,
  CreditCard,
  Phone,
  Mail,
  MapPin,
} from 'lucide-angular';

@Component({
  selector: 'app-footer',
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css',
})
export class FooterComponent {
  readonly Truck = Truck;
  readonly RotateCcw = RotateCcw;
  readonly ShieldCheck = ShieldCheck;
  readonly CreditCard = CreditCard;
  readonly Phone = Phone;
  readonly Mail = Mail;
  readonly MapPin = MapPin;
}
