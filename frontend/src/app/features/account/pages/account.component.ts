import { Component, computed, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  User,
  KeyRound,
  MapPin,
  Ticket,
  LogOut,
  ChevronRight,
  Lock,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmLabelDirective } from '@shared/ui/label';
import { finalize } from 'rxjs';
import { ToastService } from '@core/services/toast.service';
import { UserService } from '@core/services/user.service';
import { AuthService } from '@core/auth/auth.service';
import { AddressBookComponent } from '../components/address-book.component';
import { MyVouchersComponent } from '../components/my-vouchers.component';

const ROLE_LABEL: Record<string, string> = {
  ADMIN: 'Quản trị viên',
  STAFF: 'Nhân viên',
  CUSTOMER: 'Khách hàng',
  SUPER_ADMIN: 'Quản trị viên',
};

type AccountTab = 'profile' | 'password' | 'address' | 'vouchers';

@Component({
  selector: 'app-account',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmLabelDirective,
    AddressBookComponent,
    MyVouchersComponent,
  ],
  templateUrl: './account.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './account.component.css',
})
export class AccountComponent {
  readonly User = User;
  readonly KeyRound = KeyRound;
  readonly MapPin = MapPin;
  readonly Ticket = Ticket;
  readonly LogOut = LogOut;
  readonly ChevronRight = ChevronRight;
  readonly Lock = Lock;
  readonly Loader2 = Loader2;

  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  activeTab = signal<AccountTab>('profile');
  isSavingProfile = signal(false);
  isSavingPassword = signal(false);

  profileForm = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: [{ value: '', disabled: true }],
    phone: ['', Validators.required],
  });

  passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor() {
    this.userService.getMe().subscribe((user) => {
      this.profileForm.patchValue({
        fullName: user.fullName,
        email: user.email,
        phone: user.phone || '',
      });
    });
  }

  get userInitials(): string {
    const name = this.authService.currentUser()?.fullName;
    if (!name) return 'U';
    return name
      .split(' ')
      .map((s) => s[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  get userRoleLabel(): string {
    const role = this.authService.currentUser()?.systemRole;
    return role ? ROLE_LABEL[role] || role : 'Khách hàng';
  }

  isStaffOrAdmin = computed(() => {
    const role = this.authService.currentUser()?.systemRole;
    return role === 'ADMIN' || role === 'STAFF' || role === 'SUPER_ADMIN';
  });

  saveProfile() {
    if (this.profileForm.invalid) return;
    this.isSavingProfile.set(true);
    this.userService
      .updateMe(this.profileForm.getRawValue())
      .pipe(finalize(() => this.isSavingProfile.set(false)))
      .subscribe({
        next: () => this.toast.success('Cập nhật thông tin thành công'),
        error: () => this.toast.error('Không thể cập nhật thông tin'),
      });
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    this.isSavingPassword.set(true);
    this.userService
      .changePassword(this.passwordForm.getRawValue())
      .pipe(finalize(() => this.isSavingPassword.set(false)))
      .subscribe({
        next: () => {
          this.toast.success('Đổi mật khẩu thành công');
          this.passwordForm.reset();
        },
        error: () => this.toast.error('Đổi mật khẩu thất bại, kiểm tra mật khẩu hiện tại'),
      });
  }

  logout() {
    this.authService.logout();
  }
}
