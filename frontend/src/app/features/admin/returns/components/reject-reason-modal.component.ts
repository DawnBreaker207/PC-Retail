import { Component, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTextareaDirective } from '@shared/ui/textarea';

@Component({
  selector: 'app-reject-reason-modal',
  standalone: true,
  imports: [FormsModule, HlmButtonDirective, HlmTextareaDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './reject-reason-modal.component.html',
})
export class RejectReasonModalComponent {
  readonly closed = output<string | null>();

  reason = signal('');

  cancel() {
    this.closed.emit(null);
  }

  submit() {
    if (!this.reason().trim()) return;
    this.closed.emit(this.reason().trim());
  }
}
