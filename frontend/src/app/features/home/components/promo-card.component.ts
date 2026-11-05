import { Component, computed, input, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { splitLinkQuery } from '@shared/utils/link-query.util';

@Component({
  selector: 'app-promo-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './promo-card.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './promo-card.component.css',
})
export class PromoCardComponent {
  title = input.required<string>();
  image = input.required<string>();
  link = input('');
  bgColor = input('');
  badgeText = input('');
  priceText = input('');

  dest = computed(() => splitLinkQuery(this.link()));
}
