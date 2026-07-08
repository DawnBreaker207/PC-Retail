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
  template: `
    <form nz-form [formGroup]="form" (ngSubmit)="onSubmit()">
      <nz-form-item>
        <nz-form-label nzFor="code" nzRequired>Mã giảm giá</nz-form-label>
        <nz-form-control nzErrorTip="Vui lòng nhập mã giảm giá">
          <input nz-input id="code" formControlName="code" placeholder="NHAPLIEN" (input)="onCodeInput()" />
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label nzFor="scopeType" nzRequired>Phạm vi áp dụng</nz-form-label>
        <nz-form-control nzErrorTip="Vui lòng chọn phạm vi">
          <nz-select id="scopeType" formControlName="scopeType" (ngModelChange)="isFreeship() ? form.patchValue({ discountType: 'FIXED' }) : null">
            <nz-option nzValue="STOREWIDE" nzLabel="Toàn cửa hàng"></nz-option>
            <nz-option nzValue="CATEGORY" nzLabel="Theo danh mục"></nz-option>
            <nz-option nzValue="PRODUCT" nzLabel="Theo sản phẩm"></nz-option>
            <nz-option nzValue="FREESHIP" nzLabel="Miễn phí vận chuyển"></nz-option>
          </nz-select>
        </nz-form-control>
      </nz-form-item>

      @if (!isFreeship()) {
        <nz-form-item>
          <nz-form-label>Loại giảm giá</nz-form-label>
          <nz-form-control>
            <nz-radio-group formControlName="discountType">
              <label nz-radio nzValue="FIXED" nzId="discountFixed" for="discountFixed">Giảm tiền mặt</label>
              <label nz-radio nzValue="PERCENT" nzId="discountPercent" for="discountPercent">Giảm phần trăm</label>
            </nz-radio-group>
          </nz-form-control>
        </nz-form-item>
      }

      <nz-form-item>
        <nz-form-label nzFor="discountValue" nzRequired>Giá trị giảm</nz-form-label>
        <nz-form-control nzErrorTip="Vui lòng nhập giá trị giảm">
          @if (isFreeship() || form.value.discountType === 'FIXED') {
            <nz-input-number id="discountValue" formControlName="discountValue" [nzMin]="0.01" [nzStep]="1000" [nzFormatter]="vndFormatter" style="width: 100%"></nz-input-number>
          } @else {
            <nz-input-number id="discountValue" formControlName="discountValue" [nzMin]="0.01" [nzMax]="100" [nzStep]="1" nzAddOnAfter="%" style="width: 100%"></nz-input-number>
          }
        </nz-form-control>
      </nz-form-item>

      @if (form.value.discountType === 'PERCENT' && !isFreeship()) {
        <nz-form-item>
          <nz-form-label nzFor="maxDiscountAmount">Giảm tối đa</nz-form-label>
          <nz-form-control>
            <nz-input-number id="maxDiscountAmount" formControlName="maxDiscountAmount" [nzMin]="0" [nzStep]="10000" [nzFormatter]="vndFormatter" style="width: 100%"></nz-input-number>
          </nz-form-control>
        </nz-form-item>
      }

      <nz-form-item>
        <nz-form-label nzFor="minOrderValue">Giá trị đơn tối thiểu</nz-form-label>
        <nz-form-control>
          <nz-input-number id="minOrderValue" formControlName="minOrderValue" [nzMin]="0" [nzStep]="10000" [nzFormatter]="vndFormatter" style="width: 100%"></nz-input-number>
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label nzFor="totalQuantity" nzRequired>Số lượng</nz-form-label>
        <nz-form-control nzErrorTip="Vui lòng nhập số lượng">
          <nz-input-number id="totalQuantity" formControlName="totalQuantity" [nzMin]="1" [nzStep]="1" style="width: 100%"></nz-input-number>
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label nzFor="validFrom" nzRequired>Thời gian hiệu lực</nz-form-label>
        <nz-form-control nzErrorTip="Ngày bắt đầu phải trước ngày kết thúc">
          <nz-date-picker
            id="validFrom"
            formControlName="validFrom"
            nzShowTime
            nzPlaceHolder="Từ ngày"
            class="!w-full"
          ></nz-date-picker>
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label nzFor="validTo" nzRequired></nz-form-label>
        <nz-form-control nzErrorTip="Vui lòng chọn ngày kết thúc">
          <nz-date-picker
            id="validTo"
            formControlName="validTo"
            nzShowTime
            nzPlaceHolder="Đến ngày"
            class="!w-full"
          ></nz-date-picker>
        </nz-form-control>
      </nz-form-item>

      <div class="flex justify-end gap-3 mt-6">
        <button nz-button nzType="default" (click)="close?.()" type="button">Hủy</button>
        <button nz-button nzType="primary" type="submit" [nzLoading]="submitting()" [disabled]="!form.valid">Tạo voucher</button>
      </div>
    </form>
  `,
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
    this.form.get('validFrom')?.valueChanges.subscribe(() => this.validateDates());
    this.form.get('validTo')?.valueChanges.subscribe(() => this.validateDates());
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
