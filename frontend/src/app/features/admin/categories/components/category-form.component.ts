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
import { Category } from '@core/models/common.model';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [ReactiveFormsModule, HlmButtonDirective, HlmInputDirective, HlmLabelDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './category-form.component.html',
})
export class CategoryFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly category = input<Category | null>(null);
  readonly allCategories = input<Category[]>([]);
  readonly saved = output<boolean>();
  readonly cancelled = output<void>();

  submitting = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    icon: ['folder', Validators.required],
    imageUrl: [''],
    parentId: [null as number | null],
  });

  get isEdit(): boolean {
    return !!this.category();
  }

  get parentOptions(): Category[] {
    const curId = this.category()?.id;
    return this.allCategories().filter((c) => c.id !== curId);
  }

  ngOnInit() {
    const c = this.category();
    if (c) {
      this.form.patchValue({
        name: c.name ?? '',
        icon: c.icon ?? 'folder',
        imageUrl: c.imageUrl ?? '',
        parentId: c.parentId ?? null,
      });
    }
  }

  cancel() {
    this.cancelled.emit();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const curCat = this.category();
    const request$ =
      this.isEdit && curCat
        ? this.http.put(ApiEndpoints.admin.categoryDetail(curCat.id), body)
        : this.http.post(ApiEndpoints.admin.categories, body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success(
          this.isEdit ? 'Cập nhật danh mục thành công' : 'Thêm danh mục thành công',
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
