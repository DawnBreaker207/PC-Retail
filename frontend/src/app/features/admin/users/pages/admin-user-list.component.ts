import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  LucideAngularModule,
  Plus,
  Search,
  Edit2,
  Key,
  Lock,
  Unlock,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import {
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { ToastService } from '@core/services/toast.service';
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
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    UserFormComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './admin-user-list.component.html',
})
export class AdminUserListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly Plus = Plus;
  readonly Search = Search;
  readonly Edit2 = Edit2;
  readonly Key = Key;
  readonly Lock = Lock;
  readonly Unlock = Unlock;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly AlertCircle = AlertCircle;
  readonly Loader2 = Loader2;

  users = signal<User[]>([]);
  loading = signal(false);
  total = signal(0);
  search = signal('');
  roleFilter = signal('');
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  // Form Modal State
  formModalUser = signal<User | null | 'create'>(null);

  // Confirmation Modal State (Password reset or Status toggle)
  confirmAction = signal<{
    title: string;
    description: string;
    confirmText: string;
    action: () => void;
  } | null>(null);

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

    this.http
      .get<UserResponse>(ApiEndpoints.admin.users, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.users.set(res.content ?? []);
          this.total.set(res.totalElements ?? 0);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  onPageChange(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.page = page;
    this.loadData();
  }

  onPageSizeChange(size: number) {
    this.size = size;
    this.page = 1;
    this.loadData();
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.total() / this.size));
  }

  openCreateModal() {
    this.formModalUser.set('create');
  }

  openEditModal(user: User) {
    this.formModalUser.set(user);
  }

  closeFormModal() {
    this.formModalUser.set(null);
  }

  onFormSaved() {
    this.closeFormModal();
    this.loadData();
  }

  promptResetPassword(user: User) {
    this.confirmAction.set({
      title: 'Đặt lại mật khẩu?',
      description: `Hệ thống sẽ tạo mật khẩu ngẫu nhiên tạm thời cho ${user.fullName} (${user.email}).`,
      confirmText: 'Đặt lại mật khẩu',
      action: () => this.executeResetPassword(user),
    });
  }

  private executeResetPassword(user: User) {
    this.http
      .post<{ tempPassword: string }>(ApiEndpoints.admin.userResetPassword(user.id), {})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.toast.success(
            `Đã đặt lại mật khẩu cho ${user.fullName}. Mật khẩu tạm: ${res.tempPassword}`,
          );
          this.closeConfirmAction();
        },
        error: () => this.toast.error('Đặt lại mật khẩu thất bại'),
      });
  }

  promptToggleStatus(user: User) {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.confirmAction.set({
      title: nextStatus === 'ACTIVE' ? 'Kích hoạt lại tài khoản?' : 'Khóa tài khoản này?',
      description:
        nextStatus === 'ACTIVE'
          ? `Tài khoản ${user.email} sẽ có thể đăng nhập lại vào hệ thống.`
          : `Người dùng ${user.email} sẽ bị tước quyền truy cập ngay lập tức.`,
      confirmText: nextStatus === 'ACTIVE' ? 'Kích hoạt' : 'Khóa tài khoản',
      action: () => this.executeToggleStatus(user, nextStatus),
    });
  }

  private executeToggleStatus(user: User, nextStatus: string) {
    this.http
      .put(ApiEndpoints.admin.userStatus(user.id), { status: nextStatus })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(
            nextStatus === 'ACTIVE' ? 'Đã kích hoạt tài khoản' : 'Đã khóa tài khoản',
          );
          this.closeConfirmAction();
          this.loadData();
        },
        error: () => this.toast.error('Thao tác thất bại'),
      });
  }

  closeConfirmAction() {
    this.confirmAction.set(null);
  }
}
