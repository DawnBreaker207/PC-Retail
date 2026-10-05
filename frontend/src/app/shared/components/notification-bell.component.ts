import { Component, inject } from '@angular/core';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NotificationService } from '@core/services/notification.service';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-notification-bell',
  imports: [NzBadgeModule, NzButtonModule, NzDropDownModule, NzIconModule, NzListModule, NzEmptyModule],
  template: `
    <button
      type="button"
      nz-button
      nzType="text"
      nz-dropdown
      [nzDropdownMenu]="notifMenu"
      nzTrigger="click"
      nzPlacement="bottomRight"
      aria-label="Notifications"
      class="!inline-flex !items-center !justify-center !w-11 !h-11 !rounded-full hover:!bg-white/10"
      (click)="onOpen()"
    >
      <nz-badge [nzCount]="notif.unreadCount()" [nzShowZero]="false">
        <span nz-icon nzType="bell" class="text-lg text-white"></span>
      </nz-badge>
    </button>
    <nz-dropdown-menu #notifMenu="nzDropdownMenu">
      <div class="!w-[320px] !max-h-[400px] !overflow-auto !bg-white dark:!bg-[#1f1f1f] !rounded-lg !shadow-lg !p-2">
        <div class="flex items-center justify-between px-2 py-1">
          <strong>Notifications</strong>
          <button nz-button nzType="link" (click)="notif.markAllRead()">Mark all read</button>
        </div>
        @if (!notif.items().length) {
          <nz-empty nzNotFoundContent="No notifications"></nz-empty>
        } @else {
          <nz-list>
            @for (n of notif.items(); track n.id) {
              <nz-list-item (click)="notif.markRead(n.id)" class="cursor-pointer">
                <nz-list-item-meta
                  [nzTitle]="n.title"
                  [nzDescription]="n.body || n.type"
                ></nz-list-item-meta>
              </nz-list-item>
            }
          </nz-list>
        }
      </div>
    </nz-dropdown-menu>
  `,
})
export class NotificationBellComponent {
  notif = inject(NotificationService);
  private auth = inject(AuthService);

  onOpen() {
    if (this.auth.isAuthenticated()) this.notif.load();
  }
}
