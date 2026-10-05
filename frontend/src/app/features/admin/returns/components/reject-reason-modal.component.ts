import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-reject-reason-modal',
  imports: [FormsModule, NzInputModule, NzButtonModule],
  templateUrl: './reject-reason-modal.component.html',
})
export class RejectReasonModalComponent {
  private modalRef = inject(NzModalRef);

  reason = '';

  cancel() {
    this.modalRef.close();
  }

  submit() {
    if (!this.reason.trim()) return;
    this.modalRef.close(this.reason.trim());
  }
}
