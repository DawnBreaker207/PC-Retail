import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-promo-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './promo-card.component.html',
  styleUrl: './promo-card.component.css',
})
export class PromoCardComponent {
  title = input.required<string>();
  image = input.required<string>();
  link = input('');
  bgColor = input('');
  badgeText = input('');
  priceText = input('');
}
