import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule, ChevronRight, Phone, Mail, MapPin, Send } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmLabelDirective } from '@shared/ui/label';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import { ToastService } from '@core/services/toast.service';

@Component({
  selector: 'app-contact',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmLabelDirective,
    HlmTextareaDirective,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './contact.component.html',
})
export class ContactComponent {
  readonly ChevronRight = ChevronRight;
  readonly Phone = Phone;
  readonly Mail = Mail;
  readonly MapPin = MapPin;
  readonly Send = Send;

  private fb = inject(FormBuilder);
  private toast = inject(ToastService);

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    phone: ['', Validators.required],
    content: ['', Validators.required],
  });

  submit() {
    if (this.form.invalid) return;
    this.toast.success('Đã gửi yêu cầu, DAWN PC Retail sẽ liên hệ lại trong thời gian sớm nhất');
    this.form.reset();
  }
}
