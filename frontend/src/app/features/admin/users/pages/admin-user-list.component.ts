import { Component, inject, DestroyRef, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User } from '@core/models/common.model';
import { UserFormComponent } from '../components/user-form.component';

interface UserResponse {
  content: User[];
  totalElements: number;
}

const ROLE_OPTIONS = [
  { value: '', label: 'Tất cả vai trò' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'STAFF', label: 'Staff' },
  { value: 'CUSTOMER', label: 'Khách hàng' },
  { value: 'SUPER_ADMIN', label: 'Super Admin' },
];

@Component({
  selector: 'app-admin-user-list',
  imports: [
    DatePipe, FormsModule, NzTableModule, NzButtonModule, NzIconModule, NzSpinModule,
    NzTagModule, NzInputModule, NzSelectModule, NzPopconfirmModule, NzModalModule,
  ],
  templateUrl: './admin-user-list.component.html',
})
export class AdminUserListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);

  users = signal<User[]>([]);
  loading = signal(false);
  total = signal(0);
  search = signal('');
  roleFilter = signal('');
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  protected readonly ROLE_OPTIONS = ROLE_OPTIONS;

  constructor() {
    this.loadData();
  }

  onSearch() {
    this.page = 1;
    this.loadData();
  }

  onRoleChange(role: string) {
    this.roleFilter.set(role);
    this.page = 1;
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams().set('page', this.page - 1).set('size', this.size);
    if (this.search()) params = params.set('search', this.search());
    if (this.roleFilter()) params = params.set('role', this.roleFilter());

    this.http.get<UserResponse>(ApiEndpoints.admin.users, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.users.set(res.content);
          this.total.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  onPageChange(page: number) {
    this.page = page;
    this.loadData();
  }

  onPageSizeChange(size: number) {
    this.size = size;
    this.page = 1;
    this.loadData();
  }

  openCreateModal() {
    this.openForm(null);
  }

  openEditModal(user: User) {
    this.openForm(user);
  }

  private openForm(user: User | null) {
    const modalRef = this.modal.create({
      nzTitle: user ? 'Sửa người dùng' : 'Thêm người dùng mới',
      nzContent: UserFormComponent,
      nzData: { user },
      nzFooter: null,
      nzWidth: 480,
    });
    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success(user ? 'Cập nhật người dùng thành công' : 'Thêm người dùng thành công');
        this.loadData();
      }
    });
  }

  resetPassword(user: User) {
    this.http.post<{ tempPassword: string }>(ApiEndpoints.admin.userResetPassword(user.id), {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.message.success(`Đã đặt lại mật khẩu cho ${user.fullName}. Mật khẩu tạm: ${res.tempPassword}`, { nzDuration: 8000 });
        },
        error: () => this.message.error('Đặt lại mật khẩu thất bại'),
      });
  }

  toggleStatus(user: User) {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.http.put(ApiEndpoints.admin.userStatus(user.id), { status: nextStatus })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success(nextStatus === 'ACTIVE' ? 'Đã kích hoạt tài khoản' : 'Đã khóa tài khoản');
          this.loadData();
        },
        error: () => this.message.error('Thao tác thất bại'),
      });
  }
}
