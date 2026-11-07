import {
  Component,
  inject,
  signal,
  HostListener,
  ElementRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { LucideAngularModule, Bell, CheckCheck, Inbox } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import { NotificationService } from '@core/services/notification.service';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-notification-bell',
  imports: [LucideAngularModule, HlmButtonDirective, HlmBadgeDirective, HlmTooltipDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="relative inline-block">
      <button
        hlmBtn
        variant="ghost"
        size="icon"
        type="button"
        [hlmTooltip]="'Thông báo hệ thống'"
        aria-label="Thông báo"
        class="relative w-10 h-10 rounded-full text-muted dark:text-muted-dark hover:text-ink dark:hover:text-ink-dark cursor-pointer"
        (click)="toggleOpen()"
      >
        <lucide-angular [img]="Bell" class="w-5 h-5" />
        @if (notif.unreadCount() > 0) {
          <span
            hlmBadge
            variant="destructive"
            class="absolute top-1 right-1 min-w-[18px] h-[18px] px-1 text-white text-[10px] font-bold rounded-full flex items-center justify-center p-0"
          >
            {{ notif.unreadCount() > 99 ? '99+' : notif.unreadCount() }}
          </span>
        }
      </button>

      @if (isOpen()) {
        <div
          class="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto bg-surface dark:bg-surface-dark border border-border/80 dark:border-border-dark/80 rounded-xl shadow-xl z-50 p-2 animate-scale-in"
        >
          <div
            class="flex items-center justify-between px-3 py-2 border-b border-border/60 dark:border-border-dark/60 mb-1"
          >
            <span class="font-bold text-sm text-ink dark:text-ink-dark">Thông báo</span>
            @if (notif.items().length > 0) {
              <button
                hlmBtn
                variant="ghost"
                size="sm"
                type="button"
                [hlmTooltip]="'Đánh dấu tất cả là đã đọc'"
                (click)="notif.markAllRead()"
                class="text-xs text-primary h-7 px-2 font-medium inline-flex items-center gap-1 cursor-pointer"
              >
                <lucide-angular [img]="CheckCheck" class="w-3.5 h-3.5" />
                Đọc tất cả
              </button>
            }
          </div>

          @if (!notif.items().length) {
            <div class="py-8 text-center text-muted dark:text-muted-dark">
              <lucide-angular [img]="Inbox" class="w-8 h-8 mx-auto mb-2 opacity-50" />
              <p class="text-xs">Chưa có thông báo nào</p>
            </div>
          } @else {
            <div class="divide-y divide-border/40 dark:divide-border-dark/40">
              @for (n of notif.items(); track n.id) {
                <div
                  (click)="markRead(n.id)"
                  class="p-2.5 hover:bg-bg/60 dark:hover:bg-bg-dark/60 rounded-lg cursor-pointer transition-colors"
                >
                  <p class="text-xs font-semibold text-ink dark:text-ink-dark line-clamp-1">
                    {{ n.title }}
                  </p>
                  <p class="text-xs text-muted dark:text-muted-dark line-clamp-2 mt-0.5">
                    {{ n.body || n.type }}
                  </p>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class NotificationBellComponent {
  readonly Bell = Bell;
  readonly CheckCheck = CheckCheck;
  readonly Inbox = Inbox;

  notif = inject(NotificationService);
  private auth = inject(AuthService);
  private el = inject(ElementRef);

  isOpen = signal(false);

  toggleOpen() {
    const next = !this.isOpen();
    this.isOpen.set(next);
    if (next && this.auth.isAuthenticated()) {
      this.notif.load();
    }
  }

  markRead(id: number) {
    this.notif.markRead(id);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.el.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }
}
