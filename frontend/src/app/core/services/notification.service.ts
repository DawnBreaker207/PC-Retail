import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of, tap } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';

export interface NotificationItem {
  id: number;
  type: string;
  title: string;
  body?: string;
  referenceId?: number;
  read: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);

  items = signal<NotificationItem[]>([]);
  unreadCount = signal(0);

  load() {
    this.http.get<NotificationItem[]>(ApiEndpoints.notifications.base).pipe(
      catchError(() => of([] as NotificationItem[])),
    ).subscribe((list) => {
      const arr = Array.isArray(list) ? list : [];
      this.items.set(arr);
      this.unreadCount.set(arr.filter((n) => !n.read).length);
    });
  }

  prepend(n: NotificationItem) {
    this.items.update((arr) => (arr.some((x) => x.id === n.id) ? arr : [n, ...arr]));
    this.unreadCount.set(this.items().filter((x) => !x.read).length);
  }

  markRead(id: number) {
    this.http.put<NotificationItem>(ApiEndpoints.notifications.read(id), {}).pipe(
      catchError(() => of(null)),
      tap(() => {
        this.items.update((arr) => arr.map((n) => (n.id === id ? { ...n, read: true } : n)));
        this.unreadCount.set(this.items().filter((n) => !n.read).length);
      }),
    ).subscribe();
  }

  markAllRead() {
    this.http.put(ApiEndpoints.notifications.readAll, {}).pipe(
      catchError(() => of(null)),
      tap(() => {
        this.items.update((arr) => arr.map((n) => ({ ...n, read: true })));
        this.unreadCount.set(0);
      }),
    ).subscribe();
  }
}
