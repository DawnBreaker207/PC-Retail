import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CategoryMenuService {
  isOpen = signal(false);
  /** Whether the home page's persistent category sidebar is currently scrolled into view. */
  isSidebarInViewport = signal(false);

  toggle(): void {
    this.isOpen.update((v) => !v);
  }

  close(): void {
    this.isOpen.set(false);
  }
}
