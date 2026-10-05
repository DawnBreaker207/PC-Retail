import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Category } from '@core/models/common.model';

interface CategoryFormData {
  category: Category | null;
  allCategories: Category[];
}

@Component({
  selector: 'app-category-form',
  imports: [ReactiveFormsModule, NzFormModule, NzInputModule, NzSelectModule, NzButtonModule],
  templateUrl: './category-form.component.html',
})
export class CategoryFormComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  private modalRef = inject(NzModalRef);
  private data: CategoryFormData = inject(NZ_MODAL_DATA);

  submitting = signal(false);
  isEdit = !!this.data.category;

  parentOptions = this.data.allCategories.filter((c) => c.id !== this.data.category?.id);

  form = this.fb.nonNullable.group({
    name: [this.data.category?.name ?? '', Validators.required],
    icon: [this.data.category?.icon ?? 'appstore', Validators.required],
    imageUrl: [this.data.category?.imageUrl ?? ''],
    parentId: [this.data.category?.parentId ?? null],
  });

  cancel() {
    this.modalRef.close();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const body = this.form.getRawValue();
    const request$ = this.isEdit
      ? this.http.put(ApiEndpoints.admin.categoryDetail(this.data.category!.id), body)
      : this.http.post(ApiEndpoints.admin.categories, body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalRef.close(true);
      },
      error: () => {
        this.submitting.set(false);
        this.message.error('Thao tác thất bại');
      },
    });
  }
}
