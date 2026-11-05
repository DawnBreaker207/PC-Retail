import {
  Component,
  inject,
  DestroyRef,
  signal,
  OnInit,
  output,
  ChangeDetectionStrategy,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-voucher-form',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, HlmButtonDirective, HlmInputDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './voucher-form.component.html',
})
export class VoucherFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly saved = output<boolean>();
  readonly cancelled = output<void>();

  submitting = signal(false);

  form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    scopeType: ['STOREWIDE', [Validators.required]],
    discountType: ['FIXED'],
    discountValue: [0, [Validators.required, Validators.min(0.01)]],
    maxDiscountAmount: [0],
    minOrderValue: [0],
    totalQuantity: [1, [Validators.required, Validators.min(1)]],
    validFrom: ['', [Validators.required]],
    validTo: ['', [Validators.required]],
  });

  ngOnInit() {
    this.form
      .get('validFrom')
      ?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.validateDates());
    this.form
      .get('validTo')
      ?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.validateDates());
  }

  onCodeInput() {
    const control = this.form.get('code');
    if (control?.value) {
      control.setValue(control.value.toUpperCase().replace(/[^A-Z0-9_-]/g, ''), {
        emitEvent: false,
      });
    }
  }

  isFreeship(): boolean {
    return this.form.value.scopeType === 'FREESHIP';
  }

  private validateDates() {
    const from = this.form.get('validFrom')?.value;
    const to = this.form.get('validTo')?.value;
    if (from && to && new Date(from) >= new Date(to)) {
      this.form.get('validTo')?.setErrors({ dateOrder: true });
    } else {
      this.form.get('validTo')?.setErrors(null);
    }
  }

  cancel() {
    this.cancelled.emit();
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.submitting.set(true);

    const body = {
      ...this.form.value,
      discountType: this.isFreeship() ? 'FIXED' : this.form.value.discountType,
    };

    this.http
      .post(ApiEndpoints.promotion.vouchers, body)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Tạo voucher thành công');
          this.saved.emit(true);
        },
        error: () => {
          this.submitting.set(false);
          this.toast.error('Tạo voucher thất bại');
        },
      });
  }
}
