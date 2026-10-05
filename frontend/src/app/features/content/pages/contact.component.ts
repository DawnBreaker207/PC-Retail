import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'app-contact',
  imports: [
    RouterLink, ReactiveFormsModule, NzBreadCrumbModule,
    NzFormModule, NzInputModule, NzButtonModule, NzIconModule,
  ],
  templateUrl: './contact.component.html',
})
export class ContactComponent {
  private fb = inject(FormBuilder);
  private message = inject(NzMessageService);

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    phone: ['', Validators.required],
    content: ['', Validators.required],
  });

  submit() {
    if (this.form.invalid) return;
    this.message.success('Đã gửi yêu cầu, DAWN PC Retail sẽ liên hệ lại trong thời gian sớm nhất');
    this.form.reset();
  }
}
