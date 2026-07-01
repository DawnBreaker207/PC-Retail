import { Injectable, inject } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConfirmModalService {
  private modal = inject(NzModalService);

  confirm(title: string, content: string): Observable<boolean> {
    return new Observable((observer) => {
      this.modal.confirm({
        nzTitle: title,
        nzContent: content,
        nzOnOk: () => {
          observer.next(true);
          observer.complete();
        },
        nzOnCancel: () => {
          observer.next(false);
          observer.complete();
        },
      });
    });
  }
}
