import {
  Directive,
  ElementRef,
  HostListener,
  inject,
  input,
  OnDestroy,
  Renderer2,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

let nextUniqueId = 0;

@Directive({
  selector: '[hlmTooltip]',
  standalone: true,
})
export class HlmTooltipDirective implements OnDestroy {
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly tooltipText = input<string>('', { alias: 'hlmTooltip' });
  readonly position = input<'top' | 'bottom' | 'left' | 'right'>('top', { alias: 'tooltipPosition' });
  readonly disabled = input<boolean>(false, { alias: 'tooltipDisabled' });

  private tooltipElement: HTMLElement | null = null;
  private readonly tooltipId = `hlm-tooltip-${nextUniqueId++}`;
  private showTimeout: any = null;

  @HostListener('mouseenter')
  onMouseEnter(): void {
    this.scheduleShow();
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    this.hide();
  }

  @HostListener('focusin')
  onFocus(): void {
    this.scheduleShow();
  }

  @HostListener('focusout')
  onBlur(): void {
    this.hide();
  }

  @HostListener('keydown.escape')
  onEscape(): void {
    this.hide();
  }

  private scheduleShow(): void {
    if (!this.isBrowser || this.disabled() || !this.tooltipText()?.trim()) return;
    this.clearTimeout();
    this.showTimeout = setTimeout(() => {
      this.show();
    }, 120);
  }

  private show(): void {
    if (this.tooltipElement || !this.isBrowser) return;

    const text = this.tooltipText()?.trim();
    if (!text) return;

    // Create tooltip container
    const tip = this.renderer.createElement('div');
    this.renderer.setAttribute(tip, 'id', this.tooltipId);
    this.renderer.setAttribute(tip, 'role', 'tooltip');

    // Shadcn / Spartan styling classes
    const classes = [
      'fixed',
      'z-[9999]',
      'pointer-events-none',
      'px-2.5',
      'py-1',
      'text-[11px]',
      'font-medium',
      'leading-tight',
      'rounded-md',
      'shadow-md',
      'border',
      'border-border/80',
      'bg-foreground',
      'text-background',
      'dark:bg-surface-dark',
      'dark:text-foreground',
      'dark:border-border-dark',
      'transition-opacity',
      'duration-150',
      'opacity-0',
      'max-w-xs',
      'text-center',
      'whitespace-normal',
      'break-words',
    ];
    classes.forEach((c) => this.renderer.addClass(tip, c));
    this.renderer.setProperty(tip, 'textContent', text);

    this.renderer.appendChild(document.body, tip);
    this.tooltipElement = tip;

    // Calculate position
    this.updatePosition(tip);

    // Make visible
    requestAnimationFrame(() => {
      if (this.tooltipElement) {
        this.renderer.removeClass(this.tooltipElement, 'opacity-0');
        this.renderer.addClass(this.tooltipElement, 'opacity-100');
      }
    });

    // ARIA link
    this.renderer.setAttribute(this.el.nativeElement, 'aria-describedby', this.tooltipId);
  }

  private updatePosition(tip: HTMLElement): void {
    const hostRect = this.el.nativeElement.getBoundingClientRect();
    const tipRect = tip.getBoundingClientRect();
    const pos = this.position();
    const gap = 6;

    let top = 0;
    let left = 0;

    switch (pos) {
      case 'bottom':
        top = hostRect.bottom + gap;
        left = hostRect.left + (hostRect.width - tipRect.width) / 2;
        break;
      case 'left':
        top = hostRect.top + (hostRect.height - tipRect.height) / 2;
        left = hostRect.left - tipRect.width - gap;
        break;
      case 'right':
        top = hostRect.top + (hostRect.height - tipRect.height) / 2;
        left = hostRect.right + gap;
        break;
      case 'top':
      default:
        top = hostRect.top - tipRect.height - gap;
        left = hostRect.left + (hostRect.width - tipRect.width) / 2;
        break;
    }

    // Viewport bound clamping
    const padding = 8;
    const maxLeft = window.innerWidth - tipRect.width - padding;
    const maxTop = window.innerHeight - tipRect.height - padding;

    left = Math.max(padding, Math.min(left, maxLeft));
    top = Math.max(padding, Math.min(top, maxTop));

    this.renderer.setStyle(tip, 'top', `${Math.round(top)}px`);
    this.renderer.setStyle(tip, 'left', `${Math.round(left)}px`);
  }

  private hide(): void {
    this.clearTimeout();
    if (this.tooltipElement) {
      this.renderer.removeChild(document.body, this.tooltipElement);
      this.tooltipElement = null;
      this.renderer.removeAttribute(this.el.nativeElement, 'aria-describedby');
    }
  }

  private clearTimeout(): void {
    if (this.showTimeout) {
      clearTimeout(this.showTimeout);
      this.showTimeout = null;
    }
  }

  ngOnDestroy(): void {
    this.hide();
  }
}
