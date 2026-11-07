import {
  Component,
  HostListener,
  inject,
  signal,
  PLATFORM_ID,
  ChangeDetectionStrategy,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Phone, MessageCircle, Wrench, ChevronUp } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTooltipDirective } from '@shared/ui/tooltip';

@Component({
  selector: 'app-floating-support',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective, HlmTooltipDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <aside
      aria-label="Tiện ích liên hệ nhanh"
      class="fixed bottom-6 right-4 sm:right-6 z-40 flex flex-col items-center gap-2.5 select-none print:hidden"
    >
      <!-- Xây dựng cấu hình PC nhanh -->
      <a
        routerLink="/pc-builder/custom"
        class="group relative flex items-center justify-center w-11 h-11 rounded-full bg-surface dark:bg-surface-dark border border-border/80 dark:border-border-dark/80 text-primary shadow-md hover:shadow-lg hover:border-primary/50 hover:scale-105 active:scale-95 transition-all"
        title="Xây dựng cấu hình PC tự chọn"
      >
        <lucide-angular
          [img]="WrenchIcon"
          class="w-5 h-5 text-primary group-hover:rotate-45 transition-transform"
        />
        <span
          class="pointer-events-none absolute right-full mr-2.5 px-2.5 py-1 rounded-lg bg-surface-dark dark:bg-surface text-surface text-[11px] font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity shadow-md"
        >
          Build PC theo yêu cầu
        </span>
      </a>

      <!-- Chat Zalo / Kỹ thuật viên hỗ trợ -->
      <a
        href="https://zalo.me/0900000000"
        target="_blank"
        rel="noopener noreferrer"
        class="group relative flex items-center justify-center w-12 h-12 rounded-full bg-blue-600 text-white shadow-md shadow-blue-500/25 hover:shadow-lg hover:bg-blue-700 hover:scale-105 active:scale-95 transition-all"
        title="Chat Zalo tư vấn phần cứng 24/7"
      >
        <lucide-angular [img]="MessageCircleIcon" class="w-5 h-5" />
        <span
          class="pointer-events-none absolute right-full mr-2.5 px-2.5 py-1 rounded-lg bg-surface-dark dark:bg-surface text-surface text-[11px] font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity shadow-md"
        >
          Chat Zalo tư vấn 24/7
        </span>
      </a>

      <!-- Gọi Hotline nhanh -->
      <a
        href="tel:19005301"
        class="group relative flex items-center justify-center w-12 h-12 rounded-full bg-primary text-white shadow-lg shadow-primary/30 hover:shadow-xl hover:bg-primary-dark hover:scale-105 active:scale-95 transition-all"
        title="Hotline tư vấn: 1900 5301"
      >
        <lucide-angular [img]="PhoneIcon" class="w-5 h-5 group-hover:animate-bounce text-white" />
        <span class="absolute -top-1 -right-1 flex h-3.5 w-3.5">
          <span
            class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"
          ></span>
          <span
            class="relative inline-flex rounded-full h-3.5 w-3.5 bg-red-500 border-2 border-white dark:border-black"
          ></span>
        </span>
        <span
          class="pointer-events-none absolute right-full mr-2.5 px-2.5 py-1 rounded-lg bg-surface-dark dark:bg-surface text-surface text-[11px] font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity shadow-md"
        >
          Hotline: 1900 5301
        </span>
      </a>

      <!-- Nút Cuộn lên đầu trang (Back to top) -->
      @if (showBackToTop()) {
        <button
          hlmBtn
          variant="outline"
          size="icon"
          type="button"
          (click)="scrollToTop()"
          aria-label="Cuộn lên đầu trang"
          [hlmTooltip]="'Cuộn lên đầu trang'"
          tooltipPosition="left"
          class="w-10 h-10 rounded-full bg-surface/90 dark:bg-surface-dark/90 backdrop-blur-md border border-border/80 dark:border-border-dark/80 text-foreground hover:bg-primary hover:text-white hover:border-primary shadow-md cursor-pointer"
        >
          <lucide-angular [img]="ChevronUpIcon" class="w-5 h-5" />
        </button>
      }
    </aside>
  `,
})
export class FloatingSupportComponent {
  private platformId = inject(PLATFORM_ID);
  readonly WrenchIcon = Wrench;
  readonly MessageCircleIcon = MessageCircle;
  readonly PhoneIcon = Phone;
  readonly ChevronUpIcon = ChevronUp;

  showBackToTop = signal(false);

  @HostListener('window:scroll')
  onWindowScroll(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.showBackToTop.set(window.scrollY > 300);
    }
  }

  scrollToTop(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
