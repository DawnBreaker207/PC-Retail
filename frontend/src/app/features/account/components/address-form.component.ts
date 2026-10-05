import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { UserService } from '@core/services/user.service';
import { Address } from '@core/models/common.model';

interface AddressFormData {
  address: Address | null;
}

@Component({
  selector: 'app-address-form',
  imports: [ReactiveFormsModule, NzFormModule, NzInputModule, NzButtonModule],
  templateUrl: './address-form.component.html',
})
export class AddressFormComponent {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  private modalRef = inject(NzModalRef);
  private data: AddressFormData = inject(NZ_MODAL_DATA);

  submitting = signal(false);
  isEdit = !!this.data.address;

  form = this.fb.nonNullable.group({
    label: [this.data.address?.label ?? '', Validators.required],
    fullName: [this.data.address?.fullName ?? '', Validators.required],
    phone: [this.data.address?.phone ?? '', Validators.required],
    street: [this.data.address?.street ?? '', Validators.required],
    district: [this.data.address?.district ?? '', Validators.required],
    city: [this.data.address?.city ?? '', Validators.required],
  });

  cancel() {
    this.modalRef.close();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const request$ = this.isEdit
      ? this.userService.updateAddress(this.data.address!.id, body)
      : this.userService.addAddress(body);

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
