import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzMessageService } from 'ng-zorro-antd/message';
import { finalize } from 'rxjs';
import { UserService } from '@core/services/user.service';
import { AuthService } from '@core/auth/auth.service';
import { AddressBookComponent } from '../components/address-book.component';
import { MyVouchersComponent } from '../components/my-vouchers.component';

const ROLE_LABEL: Record<string, string> = {
  ADMIN: 'Quản trị viên', STAFF: 'Nhân viên', CUSTOMER: 'Khách hàng', SUPER_ADMIN: 'Quản trị viên',
};

@Component({
  selector: 'app-account',
  imports: [
    RouterLink, ReactiveFormsModule, NzBreadCrumbModule, NzTabsModule,
    NzFormModule, NzInputModule, NzButtonModule, NzIconModule, NzTagModule,
    NzToolTipModule, AddressBookComponent, MyVouchersComponent,
  ],
  templateUrl: './account.component.html',
  styleUrl: './account.component.css',
})
export class AccountComponent {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private message = inject(NzMessageService);

  isSavingProfile = signal(false);
  isSavingPassword = signal(false);
  tabPosition = signal<'left' | 'top'>(typeof window !== 'undefined' && window.innerWidth < 768 ? 'top' : 'left');

  @HostListener('window:resize')
  onResize() {
    this.tabPosition.set(window.innerWidth < 768 ? 'top' : 'left');
  }

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
    return name.split(' ').map((s) => s[0]).join('').toUpperCase().slice(0, 2);
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
      .subscribe(() => this.message.success('Cập nhật thông tin thành công'));
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    this.isSavingPassword.set(true);
    this.userService
      .changePassword(this.passwordForm.getRawValue())
      .pipe(finalize(() => this.isSavingPassword.set(false)))
      .subscribe(() => {
        this.message.success('Đổi mật khẩu thành công');
        this.passwordForm.reset();
      });
  }

  logout() {
    this.authService.logout();
  }
}
