import { Injectable, signal } from '@angular/core';

export interface ToastItem {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<ToastItem[]>([]);

  show(message: string, type: ToastItem['type'] = 'info', duration = 3500) {
    const id = Math.random().toString(36).substring(2, 9);
    this.toasts.update((t) => [...t, { id, type, message }]);
    if (typeof window !== 'undefined') {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  success(msg: string) {
    this.show(msg, 'success');
  }

  error(msg: string) {
    this.show(msg, 'error');
  }

  warning(msg: string) {
    this.show(msg, 'warning');
  }

  info(msg: string) {
    this.show(msg, 'info');
  }

  dismiss(id: string) {
    this.toasts.update((t) => t.filter((x) => x.id !== id));
  }
}
