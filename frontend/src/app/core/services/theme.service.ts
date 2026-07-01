import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  isDark = signal(false);

  toggle() {
    this.isDark.update((v) => !v);
    document.documentElement.classList.toggle('dark', this.isDark());
    localStorage.setItem('theme', this.isDark() ? 'dark' : 'light');
  }

  init() {
    const saved = localStorage.getItem('theme');
    const isDark = saved === 'dark';
    this.isDark.set(isDark);
    document.documentElement.classList.toggle('dark', isDark);
  }
}
