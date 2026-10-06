import { Injectable, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  isDark = signal(false);

  toggle() {
    this.isDark.update((v) => !v);
    if (!this.isBrowser) return;
    document.documentElement.classList.toggle('dark', this.isDark());
    localStorage.setItem('theme', this.isDark() ? 'dark' : 'light');
  }

  init() {
    if (!this.isBrowser) return;
    const saved = localStorage.getItem('theme');
    const isDark = saved === 'dark';
    this.isDark.set(isDark);
    document.documentElement.classList.toggle('dark', isDark);
  }
}
