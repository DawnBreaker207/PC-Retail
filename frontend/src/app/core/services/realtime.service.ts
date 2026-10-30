import { Injectable, inject, signal, OnDestroy } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthService } from '@core/auth/auth.service';
import { NotificationService, NotificationItem } from './notification.service';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class RealtimeService implements OnDestroy {
  private auth = inject(AuthService);
  private notif = inject(NotificationService);

  private client: Client | null = null;
  connected = signal(false);

  connect() {
    if (this.client?.active) return;
    if (typeof window === 'undefined') return; // No sockets during SSR.
    const user = this.auth.currentUser();
    const token = this.auth.getToken();
    if (!user || !token) return;
    const base = environment.apiUrl || window.location.origin;
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${base}/ws`) as WebSocket,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000 + Math.floor(Math.random() * 2000), // Jitter to prevent thundering herd
      onConnect: () => {
        this.connected.set(true);
        this.client?.subscribe(`/topic/notifications/${user.id}`, (msg) => {
          try {
            const n = JSON.parse(msg.body) as NotificationItem;
            this.notif.prepend(n);
          } catch {
            this.notif.load();
          }
        });
      },
      onDisconnect: () => this.connected.set(false),
      onStompError: () => this.connected.set(false),
    });
    this.client.activate();
  }

  disconnect() {
    this.connected.set(false);
    this.client?.deactivate();
    this.client = null;
  }

  ngOnDestroy() {
    this.disconnect();
  }
}
