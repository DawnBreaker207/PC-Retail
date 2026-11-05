import {
  Component,
  DestroyRef,
  inject,
  input,
  output,
  OnInit,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmLabelDirective } from '@shared/ui/label';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { User } from '@core/models/common.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, HlmButtonDirective, HlmInputDirective, HlmLabelDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './user-form.component.html',
})
export class UserFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly user = input<User | null>(null);
  readonly saved = output<boolean>();
  readonly cancelled = output<void>();

  submitting = signal(false);

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', Validators.required],
    systemRole: ['CUSTOMER', Validators.required],
  });

  get isEdit(): boolean {
    return !!this.user();
  }

  ngOnInit() {
    const u = this.user();
    if (u) {
      this.form.patchValue({
        fullName: u.fullName ?? '',
        email: u.email ?? '',
        phone: u.phone ?? '',
        systemRole: u.systemRole ?? 'CUSTOMER',
      });
      this.form.controls.email.disable();
    }
  }

  cancel() {
    this.cancelled.emit();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const curUser = this.user();
    const request$ =
      this.isEdit && curUser
        ? this.http.put(ApiEndpoints.admin.userDetail(curUser.id), body)
        : this.http.post(ApiEndpoints.admin.users, body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success(
          this.isEdit ? 'Cập nhật người dùng thành công' : 'Thêm người dùng thành công',
        );
        this.saved.emit(true);
      },
      error: () => {
        this.submitting.set(false);
        this.toast.error('Thao tác thất bại');
      },
    });
  }
}
