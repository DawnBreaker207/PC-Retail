import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User } from '@core/models/common.model';

interface UserFormData {
  user: User | null;
}

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, NzFormModule, NzInputModule, NzSelectModule, NzButtonModule],
  templateUrl: './user-form.component.html',
})
export class UserFormComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  private modalRef = inject(NzModalRef);
  private data: UserFormData = inject(NZ_MODAL_DATA);

  submitting = signal(false);
  isEdit = !!this.data.user;

  form = this.fb.nonNullable.group({
    fullName: [this.data.user?.fullName ?? '', Validators.required],
    email: [{ value: this.data.user?.email ?? '', disabled: this.isEdit }, [Validators.required, Validators.email]],
    phone: [this.data.user?.phone ?? '', Validators.required],
    systemRole: [this.data.user?.systemRole ?? 'CUSTOMER', Validators.required],
  });

  cancel() {
    this.modalRef.close();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const request$ = this.isEdit
      ? this.http.put(ApiEndpoints.admin.userDetail(this.data.user!.id), body)
      : this.http.post(ApiEndpoints.admin.users, body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalRef.close(true);
      },
      error: () => {
        this.submitting.set(false);
        this.message.error('Thao tác thất bại');
      },
    });
  }
}
