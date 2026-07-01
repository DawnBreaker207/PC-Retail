import { Component, inject, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzCarouselComponent, NzCarouselModule } from 'ng-zorro-antd/carousel';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-hero-banner',
  imports: [RouterLink, NzCarouselModule, NzButtonModule, NzIconModule],
  template: `
    <div class="hidden lg:block bg-bg dark:bg-bg-dark pt-6 lg:pt-8">
      <div class="max-w-screen-2xl mx-auto px-4">
        <div class="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-4">
          <div class="rounded-xl overflow-hidden border border-border/80 dark:border-border-dark/80 shadow-sm relative">
            <nz-carousel #desktopCarousel nzAutoPlay nzAutoPlaySpeed="5000" (nzAfterChange)="onSlideChange($event)">
              @for (slide of slides; track slide.title; let i = $index) {
                <div class="relative h-[300px] xl:h-[380px]">
                  <img [src]="slide.image" alt="{{ slide.title }}" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/30 to-transparent"></div>
                  <div class="absolute left-0 top-0 bottom-0 flex flex-col justify-center p-8 xl:p-12 max-w-lg">
                    <p class="text-primary font-semibold text-[11px] tracking-[0.15em] uppercase mb-2">{{ slide.tag }}</p>
                    <h2 class="text-white text-xl xl:text-2xl font-bold leading-tight mb-2">{{ slide.title }}</h2>
                    <p class="text-white/70 text-sm mb-4 line-clamp-2">{{ slide.subtitle }}</p>
                    <a [routerLink]="slide.ctaLink" nz-button class="!bg-primary !border-primary hover:!bg-primary-dark !text-white !rounded-lg !px-5 !text-sm !font-semibold !shadow-sm hover:!shadow-md !transition-all !w-fit">
                      {{ slide.ctaLabel }}
                      <span nz-icon nzType="right" nzTheme="outline"></span>
                    </a>
                  </div>
                </div>
              }
            </nz-carousel>
            <div class="absolute bottom-3 right-4 z-20 flex items-center gap-1.5">
              @for (slide of slides; track slide.title; let i = $index) {
                <button
                  class="w-8 h-1.5 rounded-full transition-all duration-300"
                  [class.bg-primary]="i === activeIndex"
                  [class.bg-white/50]="i !== activeIndex"
                  [class.w-8]="i !== activeIndex"
                  (click)="goTo(i)"
                  [attr.aria-label]="'Slide ' + (i + 1)"
                >
                  <span class="sr-only">Slide {{ i + 1 }}</span>
                </button>
              }
            </div>
          </div>

          <div class="hidden lg:flex flex-col gap-4">
            @for (banner of promoBanners; track banner.id) {
              <a
                [routerLink]="banner.link"
                class="flex-1 rounded-xl overflow-hidden group cursor-pointer border border-border/80 dark:border-border-dark/80 shadow-sm"
              >
                <div class="w-full h-full relative">
                  <img [src]="banner.imageUrl" alt="{{ banner.title }}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  @if (banner.subtitle) {
                    <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-3">
                      <p class="text-white font-bold text-sm">{{ banner.title }}</p>
                      <p class="text-primary text-xs font-medium">{{ banner.subtitle }}</p>
                    </div>
                  }
                </div>
              </a>
            }
          </div>
        </div>
      </div>
    </div>

    <div class="lg:hidden bg-bg dark:bg-bg-dark pt-4">
      <div class="px-4">
        <div class="rounded-xl overflow-hidden border border-border/80 dark:border-border-dark/80 shadow-sm relative">
          <nz-carousel #mobileCarousel nzAutoPlay nzAutoPlaySpeed="5000" (nzAfterChange)="onSlideChange($event)">
            @for (slide of slides; track slide.title) {
              <div class="relative h-[200px]">
                <img [src]="slide.image" alt="{{ slide.title }}" class="w-full h-full object-cover" />
                <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/30 to-transparent"></div>
                <div class="absolute left-0 top-0 bottom-0 flex flex-col justify-center p-5 max-w-xs">
                  <p class="text-primary font-semibold text-[10px] tracking-[0.15em] uppercase mb-1">{{ slide.tag }}</p>
                  <h2 class="text-white text-base font-bold leading-tight mb-1">{{ slide.title }}</h2>
                  <p class="text-white/70 text-xs mb-2 line-clamp-2">{{ slide.subtitle }}</p>
                  <a [routerLink]="slide.ctaLink" nz-button class="!bg-primary !border-primary !text-white !rounded-lg !px-4 !text-xs !font-semibold !h-8">
                    {{ slide.ctaLabel }}
                  </a>
                </div>
              </div>
            }
          </nz-carousel>
          <div class="absolute bottom-2 right-3 z-20 flex items-center gap-1.5">
            @for (slide of slides; track slide.title; let i = $index) {
              <button
                class="w-6 h-1 rounded-full transition-all duration-300"
                [class.bg-primary]="i === activeIndex"
                [class.bg-white/50]="i !== activeIndex"
                (click)="goTo(i)"
                [attr.aria-label]="'Slide ' + (i + 1)"
              >
                <span class="sr-only">Slide {{ i + 1 }}</span>
              </button>
            }
          </div>
        </div>
      </div>
    </div>
  `,
})
export class HeroBannerComponent {
  private mock = inject(MockDataService);

  slides = this.mock.getHeroSlides();
  promoBanners = this.mock.getPromoBanners();

  carousel = viewChild(NzCarouselComponent);

  activeIndex = 0;

  onSlideChange(index: number) {
    this.activeIndex = index;
  }

  goTo(index: number) {
    const c = this.carousel();
    if (c) {
      c.goTo(index);
    }
    this.activeIndex = index;
  }
}
