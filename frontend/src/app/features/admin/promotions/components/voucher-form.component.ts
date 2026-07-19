import { Component, inject, DestroyRef, signal, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-voucher-form',
  imports: [
    FormsModule, ReactiveFormsModule,
    NzFormModule, NzInputModule, NzSelectModule, NzRadioModule, NzInputNumberModule,
    NzDatePickerModule, NzButtonModule,
  ],
  templateUrl: './voucher-form.component.html',
  styleUrl: './voucher-form.component.css',
})
export class VoucherFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  submitting = signal(false);
  close?: () => void;

  form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    scopeType: ['STOREWIDE', [Validators.required]],
    discountType: ['FIXED'],
    discountValue: [0, [Validators.required, Validators.min(0.01)]],
    maxDiscountAmount: [0],
    minOrderValue: [0],
    totalQuantity: [1, [Validators.required, Validators.min(1)]],
    validFrom: [null as Date | null, [Validators.required]],
    validTo: [null as Date | null, [Validators.required]],
  });

  ngOnInit() {
    this.form.get('validFrom')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.validateDates());
    this.form.get('validTo')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.validateDates());
  }

  onCodeInput() {
    const control = this.form.get('code');
    if (control?.value) {
      control.setValue(control.value.toUpperCase().replace(/[^A-Z0-9_-]/g, ''), { emitEvent: false });
    }
  }

  isFreeship(): boolean {
    return this.form.value.scopeType === 'FREESHIP';
  }

  private validateDates() {
    const from = this.form.get('validFrom')?.value;
    const to = this.form.get('validTo')?.value;
    if (from && to && from >= to) {
      this.form.get('validTo')?.setErrors({ dateOrder: true });
    } else {
      this.form.get('validTo')?.updateValueAndValidity({ onlySelf: true });
    }
  }

  vndFormatter = (value: number) => {
    return value?.toLocaleString('vi-VN') + '₫' || '0₫';
  };

  onSubmit() {
    if (this.form.invalid) return;
    this.submitting.set(true);

    const body = {
      ...this.form.value,
      discountType: this.isFreeship() ? 'FIXED' : this.form.value.discountType,
    };

    this.http.post(ApiEndpoints.promotion.vouchers, body)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.close?.();
        },
        error: () => {
          this.submitting.set(false);
          this.message.error('Tạo voucher thất bại');
        },
      });
  }
}
