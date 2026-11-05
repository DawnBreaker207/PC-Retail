import {
  Component,
  DestroyRef,
  inject,
  output,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  LucideAngularModule,
  Loader2,
  CheckCircle2,
  Building2,
  Store,
  Truck,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmLabelDirective } from '@shared/ui/label';
import { HlmSeparatorDirective } from '@shared/ui/separator';
import { ShippingAddress, TaxInfo, FulfillmentType } from '@core/models/common.model';
import { AuthService } from '@core/auth/auth.service';
import { CheckoutService } from '@core/services/checkout.service';
import { ToastService } from '@core/services/toast.service';

export interface ShippingStepResult {
  address: ShippingAddress;
  fulfillmentType: FulfillmentType;
  invoiceRequested: boolean;
  taxInfo?: TaxInfo;
}

@Component({
  selector: 'app-shipping-form',
  standalone: true,
  imports: [ReactiveFormsModule, LucideAngularModule, HlmButtonDirective, HlmInputDirective, HlmLabelDirective, HlmSeparatorDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './shipping-form.component.html',
})
export class ShippingFormComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private checkoutService = inject(CheckoutService);
  private toast = inject(ToastService);
  private destroyRef = inject(DestroyRef);
  shippingComplete = output<ShippingStepResult>();

  readonly LoaderIcon = Loader2;
  readonly CheckCircleIcon = CheckCircle2;
  readonly BuildingIcon = Building2;
  readonly StoreIcon = Store;
  readonly TruckIcon = Truck;

  isGuest = () => !this.auth.isAuthenticated();
  otpSent = signal(false);
  otpVerified = signal(false);
  otpBusy = signal(false);
  challengeQ = signal('');
  cooldownLeft = signal(0);
  private challengeId = '';
  private cooldownTimer: ReturnType<typeof setInterval> | null = null;

  cities = ['Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ'];

  showrooms = [
    {
      id: 'sr-tb',
      name: 'Showroom Tân Bình (78-80 Hoàng Hoa Thám, P.12, Q. Tân Bình, TP.HCM)',
      city: 'Hồ Chí Minh',
      district: 'Quận Tân Bình',
      address: '78-80 Hoàng Hoa Thám, Phường 12',
    },
    {
      id: 'sr-q1',
      name: 'Showroom Quận 1 (905 Trần Hưng Đạo, P.1, Q.1, TP.HCM)',
      city: 'Hồ Chí Minh',
      district: 'Quận 1',
      address: '905 Trần Hưng Đạo, Phường 1',
    },
    {
      id: 'sr-td',
      name: 'Showroom Thủ Đức (1081 Kha Vạn Cân, Linh Tây, TP. Thủ Đức, TP.HCM)',
      city: 'Hồ Chí Minh',
      district: 'TP. Thủ Đức',
      address: '1081 Kha Vạn Cân, Phường Linh Tây',
    },
    {
      id: 'sr-th',
      name: 'Showroom Thái Hà (162 Thái Hà, P. Trung Liệt, Q. Đống Đa, Hà Nội)',
      city: 'Hà Nội',
      district: 'Quận Đống Đa',
      address: '162 Thái Hà, Phường Trung Liệt',
    },
    {
      id: 'sr-ltn',
      name: 'Showroom Lê Thanh Nghị (460 Lê Thanh Nghị, Bách Khoa, Q. Hai Bà Trưng, Hà Nội)',
      city: 'Hà Nội',
      district: 'Quận Hai Bà Trưng',
      address: '460 Lê Thanh Nghị, Phường Bách Khoa',
    },
  ];

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^(0[3|5|7|8|9])+([0-9]{8})$/)]],
    street: ['', Validators.required],
    district: ['', Validators.required],
    city: ['', Validators.required],
    fulfillmentType: ['DELIVERY' as FulfillmentType],
    selectedShowroom: ['sr-tb'],
    // VAT Invoice
    invoiceRequested: [false],
    companyName: [''],
    taxCode: [''],
    companyAddress: [''],
    invoiceEmail: [''],
    // OTP
    challengeAnswer: [''],
    otpCode: [''],
  });

  constructor() {
    this.form.controls.phone.valueChanges
      .pipe(debounceTime(600), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((phone) => {
        this.resetOtpState();
        if (phone && !this.form.controls.phone.invalid && this.isGuest()) {
          this.loadChallenge();
        }
      });

    this.form.controls.fulfillmentType.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((type) => {
        if (type === 'PICKUP') {
          this.onShowroomSelect(this.form.controls.selectedShowroom.value);
        }
      });
  }

  onShowroomSelect(srId: string) {
    const sr = this.showrooms.find((s) => s.id === srId);
    if (sr) {
      this.form.patchValue({
        city: sr.city,
        district: sr.district,
        street: sr.address,
      });
    }
  }

  private resetOtpState() {
    this.otpSent.set(false);
    this.otpVerified.set(false);
    this.challengeQ.set('');
    this.challengeId = '';
    this.cooldownLeft.set(0);
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
      this.cooldownTimer = null;
    }
  }

  private async loadChallenge() {
    try {
      const c = await firstValueFrom(this.checkoutService.otpChallenge());
      this.challengeId = c.challengeId;
      this.challengeQ.set(c.question);
    } catch {
      // ignore
    }
  }

  async sendOtp() {
    const phone = this.form.controls.phone.value;
    const answer = this.form.controls.challengeAnswer.value;
    if (!phone || this.form.controls.phone.invalid) {
      this.toast.warning('Nhập số điện thoại hợp lệ trước');
      return;
    }
    if (!answer) {
      this.toast.warning('Nhập kết quả phép tính chống bot trước');
      await this.loadChallenge();
      return;
    }
    try {
      this.otpBusy.set(true);
      await firstValueFrom(this.checkoutService.requestPhoneOtp(phone, this.challengeId, answer));
      this.challengeId = '';
      this.otpSent.set(true);
      this.startCooldown(60);
      this.toast.success('Đã gửi mã OTP về số điện thoại');
    } catch {
      this.challengeId = '';
      this.challengeQ.set('');
      this.form.controls.challengeAnswer.setValue('');
      await this.loadChallenge();
    } finally {
      this.otpBusy.set(false);
    }
  }

  private startCooldown(seconds: number) {
    this.cooldownLeft.set(seconds);
    if (this.cooldownTimer) clearInterval(this.cooldownTimer);
    this.cooldownTimer = setInterval(() => {
      const left = this.cooldownLeft() - 1;
      this.cooldownLeft.set(left);
      if (left <= 0 && this.cooldownTimer) {
        clearInterval(this.cooldownTimer);
        this.cooldownTimer = null;
      }
    }, 1000);
  }

  verifyOtp() {
    const phone = this.form.controls.phone.value;
    const code = this.form.controls.otpCode.value;
    if (!code) return;
    this.otpBusy.set(true);
    this.checkoutService.verifyPhoneOtp(phone, code).subscribe({
      next: (r) => {
        this.otpBusy.set(false);
        if (r.verified) {
          this.otpVerified.set(true);
          this.toast.success('Xác minh số điện thoại thành công');
        }
      },
      error: () => this.otpBusy.set(false),
    });
  }

  submit() {
    if (this.form.invalid) {
      this.toast.warning('Vui lòng điền đầy đủ thông tin giao hàng');
      return;
    }
    if (this.isGuest() && !this.otpVerified()) {
      this.toast.warning('Vui lòng xác minh số điện thoại bằng OTP trước');
      return;
    }

    const raw = this.form.getRawValue();

    if (raw.invoiceRequested) {
      if (!raw.companyName || !raw.taxCode || !raw.companyAddress) {
        this.toast.warning('Vui lòng điền đủ Tên công ty, Mã số thuế và Địa chỉ xuất hóa đơn');
        return;
      }
    }

    const taxInfo: TaxInfo | undefined = raw.invoiceRequested
      ? {
          companyName: raw.companyName,
          taxCode: raw.taxCode,
          companyAddress: raw.companyAddress,
          invoiceEmail: raw.invoiceEmail || raw.phone + '@invoice.vn',
        }
      : undefined;

    this.shippingComplete.emit({
      address: {
        fullName: raw.fullName,
        phone: raw.phone,
        street: raw.street,
        district: raw.district,
        city: raw.city,
      },
      fulfillmentType: raw.fulfillmentType,
      invoiceRequested: raw.invoiceRequested,
      taxInfo,
    });
  }
}
