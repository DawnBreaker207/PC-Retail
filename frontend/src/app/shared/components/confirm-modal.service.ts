import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConfirmModalService {
  confirm(title: string, content: string): Observable<boolean> {
    if (typeof window === 'undefined') return of(false);
    const result = window.confirm(`${title}\n\n${content}`);
    return of(result);
  }
}
