import { Component, DestroyRef, inject, output, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ShippingAddress } from '@core/models/common.model';
import { AuthService } from '@core/auth/auth.service';
import { CheckoutService } from '@core/services/checkout.service';

@Component({
  selector: 'app-shipping-form',
  imports: [
    ReactiveFormsModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
    NzRadioModule,
    NzButtonModule,
  ],
  templateUrl: './shipping-form.component.html',
})
export class ShippingFormComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private checkoutService = inject(CheckoutService);
  private msg = inject(NzMessageService);
  private destroyRef = inject(DestroyRef);
  shippingComplete = output<ShippingAddress>();

  // OTP SĐT bắt buộc cho guest (docs 13.5) — member bỏ qua
  isGuest = () => !this.auth.isAuthenticated();
  otpSent = signal(false);
  otpVerified = signal(false);
  otpBusy = signal(false);
  challengeQ = signal('');
  cooldownLeft = signal(0);
  private challengeId = '';
  private cooldownTimer: ReturnType<typeof setInterval> | null = null;

  cities = ['Hà Nội', 'Hồ Chí Minh', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ'];

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^(0[3|5|7|8|9])+([0-9]{8})$/)]],
    street: ['', Validators.required],
    district: ['', Validators.required],
    city: ['', Validators.required],
    fulfillmentType: ['DELIVERY' as const],
    challengeAnswer: [''],
    otpCode: [''],
  });

  constructor() {
    // Đổi SĐT thì xác minh cũ vô nghĩa — reset hết, nạp đề captcha mới khi số hợp lệ
    this.form.controls.phone.valueChanges
      .pipe(debounceTime(600), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((phone) => {
        this.resetOtpState();
        if (phone && !this.form.controls.phone.invalid && this.isGuest()) {
          this.loadChallenge();
        }
      });
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
      // rớt mạng/lỗi thì user bấm Gửi mã sẽ thử lại
    }
  }

  async sendOtp() {
    const phone = this.form.controls.phone.value;
    const answer = this.form.controls.challengeAnswer.value;
    if (!phone || this.form.controls.phone.invalid) {
      this.msg.warning('Nhập số điện thoại hợp lệ trước');
      return;
    }
    if (!answer) {
      this.msg.warning('Nhập kết quả phép tính chống bot trước');
      await this.loadChallenge();
      return;
    }
    try {
      this.otpBusy.set(true);
      await firstValueFrom(this.checkoutService.requestPhoneOtp(phone, this.challengeId, answer));
      this.challengeId = '';
      this.otpSent.set(true);
      this.startCooldown(60);
      this.msg.success('Đã gửi mã OTP về số điện thoại');
    } catch {
      // lỗi đã toast bởi interceptor; captcha dùng 1 lần nên lấy đề mới
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
          this.msg.success('Xác minh số điện thoại thành công');
        }
      },
      error: () => this.otpBusy.set(false),
    });
  }

  submit() {
    if (this.form.invalid) return;
    if (this.isGuest() && !this.otpVerified()) {
      this.msg.warning('Vui lòng xác minh số điện thoại bằng OTP trước');
      return;
    }
    const raw = this.form.getRawValue();
    this.shippingComplete.emit({ fullName: raw.fullName, phone: raw.phone, street: raw.street, district: raw.district, city: raw.city });
  }
}
