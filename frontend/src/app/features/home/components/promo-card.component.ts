import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-promo-card',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="relative rounded-xl overflow-hidden h-full group cursor-pointer border border-border/80 dark:border-border-dark/80 shadow-sm transition-shadow hover:shadow-md"
         [style.background]="bgColor() || undefined">
      <div class="absolute inset-0 opacity-15 bg-gradient-to-br from-white/40 to-transparent"></div>
      <a [routerLink]="link()" class="block w-full h-full p-4 flex flex-col justify-between relative z-10">
        @if (badgeText()) {
          <span class="self-start inline-flex items-center justify-center w-9 h-9 rounded-full bg-primary text-white text-xs font-bold shadow-lg">
            {{ badgeText() }}
          </span>
        }
        <div class="mt-auto">
          @if (priceText()) {
            <span class="inline-block bg-primary text-white text-xs font-bold px-2 py-0.5 rounded mb-1.5">{{ priceText() }}</span>
          }
          <h3 class="text-sm font-bold leading-tight text-white drop-shadow-sm">{{ title() }}</h3>
        </div>
      </a>
      <img [src]="image()" alt="" aria-hidden="true" class="absolute right-0 bottom-0 w-2/3 h-full object-contain opacity-60 pointer-events-none" />
    </div>
  `,
})
export class PromoCardComponent {
  title = input.required<string>();
  image = input.required<string>();
  link = input('');
  bgColor = input('');
  badgeText = input('');
  priceText = input('');
}
