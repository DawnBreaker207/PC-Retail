import { Component, DestroyRef, inject, signal, input, output, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule, X, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { ToastService } from '@core/services/toast.service';
import { UserService } from '@core/services/user.service';
import { Address } from '@core/models/common.model';

@Component({
  selector: 'app-address-form',
  imports: [
    ReactiveFormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
  ],
  templateUrl: './address-form.component.html',
})
export class AddressFormComponent implements OnInit {
  readonly X = X;
  readonly Loader2 = Loader2;

  address = input<Address | null>(null);
  closed = output<void>();
  saved = output<void>();

  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  submitting = signal(false);

  form = this.fb.nonNullable.group({
    label: ['', Validators.required],
    fullName: ['', Validators.required],
    phone: ['', Validators.required],
    street: ['', Validators.required],
    district: ['', Validators.required],
    city: ['', Validators.required],
  });

  ngOnInit() {
    const addr = this.address();
    if (addr) {
      this.form.patchValue({
        label: addr.label ?? '',
        fullName: addr.fullName ?? '',
        phone: addr.phone ?? '',
        street: addr.street ?? '',
        district: addr.district ?? '',
        city: addr.city ?? '',
      });
    }
  }

  cancel() {
    this.closed.emit();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const addr = this.address();
    const isEdit = !!addr;

    const request$ = isEdit
      ? this.userService.updateAddress(addr.id, body)
      : this.userService.addAddress(body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success(isEdit ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ thành công');
        this.saved.emit();
        this.closed.emit();
      },
      error: () => {
        this.submitting.set(false);
        this.toast.error('Thao tác thất bại. Vui lòng thử lại.');
      },
    });
  }
}

