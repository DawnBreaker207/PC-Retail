import { Component, ElementRef, effect, inject, signal, viewChild, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';
import { CategoryMenuService } from '@core/services/category-menu.service';
import { CategorySidebarComponent } from '@shared/components/category-sidebar.component';
import { splitLinkQuery } from '@shared/utils/link-query.util';
import { PromoBannerGridComponent } from './promo-banner-grid.component';
import { PromoCardComponent } from './promo-card.component';
import { CategoryStripComponent } from './category-strip.component';

@Component({
  selector: 'app-hero-banner',
  imports: [
    RouterLink, NzIconModule, FallbackImageDirective, CategorySidebarComponent,
    PromoBannerGridComponent, PromoCardComponent, CategoryStripComponent,
  ],
  templateUrl: './hero-banner.component.html',
  styleUrl: './hero-banner.component.css',
})
export class HeroBannerComponent implements OnDestroy {
  private mock = inject(MockDataService);
  menu = inject(CategoryMenuService);
  private categorySidebar = viewChild<ElementRef<HTMLElement>>('categorySidebar');

  slides = this.mock.getHeroSlides().map((slide) => ({ ...slide, cta: splitLinkQuery(slide.ctaLink) }));
  sideBanners = this.mock.getPromoCampaigns().slice(0, 2).map((banner) => ({ ...banner, dest: splitLinkQuery(banner.link) }));

  activeIndex = signal(0);
  private intervalId: number | undefined;
  private readonly AUTO_PLAY_MS = 5000;

  constructor() {
    // SSR: document/setInterval không tồn tại phía server — chỉ chạy trên browser
    if (typeof document === 'undefined' || typeof window === 'undefined') return;
    this.startInterval();
    this.visibilityHandler = () => {
      if (document.hidden) {
        this.pause();
      } else {
        this.resume();
      }
    };
    document.addEventListener('visibilitychange', this.visibilityHandler);

    effect(() => {
      if (this.menu.isOpen()) {
        this.categorySidebar()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });

    effect((onCleanup) => {
      const el = this.categorySidebar()?.nativeElement;
      if (!el || typeof IntersectionObserver === 'undefined') return;
      const observer = new IntersectionObserver(
        ([entry]) => this.menu.isSidebarInViewport.set(entry.isIntersecting),
        { threshold: 0.4 },
      );
      observer.observe(el);
      onCleanup(() => observer.disconnect());
    });
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
    this.menu.isSidebarInViewport.set(false);
  }
}
