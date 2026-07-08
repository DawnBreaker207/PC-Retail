import { Component, inject, signal, effect, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-hero-banner',
  imports: [RouterLink, NzIconModule, FallbackImageDirective],
  template: `
    <div class="hidden lg:block bg-bg dark:bg-bg-dark pt-6 lg:pt-8">
      <div class="max-w-screen-2xl mx-auto px-4">
        <div class="rounded-xl overflow-hidden border border-border/80 dark:border-border-dark/80 shadow-sm relative"
             role="region" aria-roledescription="carousel" aria-label="Hero banner carousel"
             (mouseenter)="pause()" (mouseleave)="resume()"
             (focusin)="pause()" (focusout)="resume()">
          <div class="relative h-[300px] xl:h-[380px] overflow-hidden will-change-transform"
               tabindex="0" role="group"
               (keydown.arrowLeft)="prev(); $event.preventDefault()"
               (keydown.arrowRight)="next(); $event.preventDefault()"
               (keydown.home)="goTo(0); $event.preventDefault()"
               (keydown.end)="goTo(slides.length - 1); $event.preventDefault()">
            @for (slide of slides; track slide.title; let i = $index) {
              <div class="absolute inset-0 transition-transform duration-500 ease-in-out"
                   [style.transform]="'translateX(' + ((i - activeIndex()) * 100) + '%)'"
                   [attr.aria-hidden]="i !== activeIndex()">
                <img [src]="slide.image" loading="lazy" appFallbackImage alt="{{ slide.title }}" class="w-full h-full object-cover" />
                <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/30 to-transparent"></div>
                <div class="absolute left-0 top-0 bottom-0 flex flex-col justify-center p-8 xl:p-12 max-w-lg">
                  <p class="text-primary font-semibold text-xs tracking-[0.15em] uppercase mb-2">{{ slide.tag }}</p>
                  <h2 class="text-white text-xl xl:text-2xl font-bold leading-tight mb-2">{{ slide.title }}</h2>
                  <p class="text-white/70 text-sm mb-4 line-clamp-2">{{ slide.subtitle }}</p>
                  <a [routerLink]="slide.ctaLink" class="inline-flex items-center gap-1.5 bg-primary hover:bg-primary-dark text-white rounded-lg px-5 text-sm font-semibold shadow-sm hover:shadow-md transition-all w-fit h-10 leading-none">
                    {{ slide.ctaLabel }}
                    <span nz-icon nzType="right" nzTheme="outline"></span>
                  </a>
                </div>
                @if (slide.discountText) {
                  <div class="absolute top-4 right-4 z-10 flex flex-col items-center">
                    <span class="bg-primary text-white text-xs font-bold px-3 py-1 rounded-full shadow-lg">{{ slide.discountText }}</span>
                  </div>
                }
              </div>
            }
          </div>
          <div class="absolute bottom-3 right-4 z-20 flex items-center gap-1.5">
            @for (slide of slides; track slide.title; let i = $index) {
              <button
                class="w-8 h-1.5 rounded-full transition-all duration-300"
                [class.bg-primary]="i === activeIndex()"
                [class.bg-white/50]="i !== activeIndex()"
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

    <div class="lg:hidden bg-bg dark:bg-bg-dark pt-4">
      <div class="px-4">
        <div class="rounded-xl overflow-hidden border border-border/80 dark:border-border-dark/80 shadow-sm relative"
             role="region" aria-roledescription="carousel" aria-label="Hero banner carousel"
             (mouseenter)="pause()" (mouseleave)="resume()"
             (focusin)="pause()" (focusout)="resume()">
          <!-- Mobile carousel -->
          <div class="relative h-[200px] overflow-hidden will-change-transform"
               tabindex="0" role="group"
               (keydown.arrowLeft)="prev(); $event.preventDefault()"
               (keydown.arrowRight)="next(); $event.preventDefault()"
               (keydown.home)="goTo(0); $event.preventDefault()"
               (keydown.end)="goTo(slides.length - 1); $event.preventDefault()">
            @for (slide of slides; track slide.title; let i = $index) {
              <div class="absolute inset-0 transition-transform duration-500 ease-in-out"
                   [style.transform]="'translateX(' + ((i - activeIndex()) * 100) + '%)'"
                   [attr.aria-hidden]="i !== activeIndex()">
                <img [src]="slide.image" loading="lazy" appFallbackImage alt="{{ slide.title }}" class="w-full h-full object-cover" />
                <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/30 to-transparent"></div>
                <div class="absolute left-0 top-0 bottom-0 flex flex-col justify-center p-5 max-w-xs">
                  <p class="text-primary font-semibold text-[11px] tracking-[0.15em] uppercase mb-1">{{ slide.tag }}</p>
                  <h2 class="text-white text-base font-bold leading-tight mb-1">{{ slide.title }}</h2>
                  <p class="text-white/70 text-xs mb-2 line-clamp-2">{{ slide.subtitle }}</p>
                  <a [routerLink]="slide.ctaLink" class="inline-flex items-center justify-center bg-primary hover:bg-primary-dark text-white rounded-lg px-4 text-xs font-semibold h-11 leading-none">
                    {{ slide.ctaLabel }}
                  </a>
                </div>
                @if (slide.discountText) {
                  <div class="absolute top-2 right-2 z-10">
                    <span class="bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow">{{ slide.discountText }}</span>
                  </div>
                }
              </div>
            }
          </div>
          <!-- Mobile dots -->
          <div class="absolute bottom-2 right-3 z-20 flex items-center gap-1.5">
            @for (slide of slides; track slide.title; let i = $index) {
              <button
                class="w-6 h-1 rounded-full transition-all duration-300"
                [class.bg-primary]="i === activeIndex()"
                [class.bg-white/50]="i !== activeIndex()"
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
export class HeroBannerComponent implements OnDestroy {
  private mock = inject(MockDataService);

  slides = this.mock.getHeroSlides();

  activeIndex = signal(0);
  private intervalId: number | undefined;
  private readonly AUTO_PLAY_MS = 5000;

  constructor() {
    this.startInterval();
    this.visibilityHandler = () => {
      if (document.hidden) {
        this.pause();
      } else {
        this.resume();
      }
    };
    document.addEventListener('visibilitychange', this.visibilityHandler);
  }

  private visibilityHandler: (() => void) | null = null;

  private startInterval() {
    this.stopInterval();
    this.intervalId = window.setInterval(() => {
      const next = (this.activeIndex() + 1) % this.slides.length;
      this.activeIndex.set(next);
    }, this.AUTO_PLAY_MS);
  }

  private stopInterval() {
    if (this.intervalId !== undefined) {
      clearInterval(this.intervalId);
      this.intervalId = undefined;
    }
  }

  goTo(index: number) {
    this.activeIndex.set(index);
    this.startInterval();
  }

  prev() {
    const prev = (this.activeIndex() - 1 + this.slides.length) % this.slides.length;
    this.activeIndex.set(prev);
    this.startInterval();
  }

  next() {
    const next = (this.activeIndex() + 1) % this.slides.length;
    this.activeIndex.set(next);
    this.startInterval();
  }

  pause() {
    this.stopInterval();
  }

  resume() {
    this.startInterval();
  }

  ngOnDestroy() {
    this.stopInterval();
    if (this.visibilityHandler) {
      document.removeEventListener('visibilitychange', this.visibilityHandler);
    }
  }
}
