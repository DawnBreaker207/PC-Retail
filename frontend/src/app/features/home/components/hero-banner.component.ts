import { Component, inject, signal, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-hero-banner',
  imports: [RouterLink, NzIconModule, FallbackImageDirective],
  templateUrl: './hero-banner.component.html',
  styleUrl: './hero-banner.component.css',
})
export class HeroBannerComponent implements OnDestroy {
  private mock = inject(MockDataService);

  slides = this.mock.getHeroSlides();
  sideBanners = this.mock.getPromoCampaigns().slice(0, 2);

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
