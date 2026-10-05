import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku } from '@core/models/common.model';

interface ProductFormData {
  product: Sku | null;
}

@Component({
  selector: 'app-product-form',
  imports: [
    ReactiveFormsModule, NzFormModule, NzInputModule, NzInputNumberModule, NzSelectModule, NzButtonModule,
  ],
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  private mock = inject(MockDataService);
  private modalRef = inject(NzModalRef);
  private data: ProductFormData = inject(NZ_MODAL_DATA);

  categories = this.mock.categories;
  brands = this.mock.brands;
  submitting = signal(false);
  isEdit = !!this.data.product;

  form = this.fb.nonNullable.group({
    name: [this.data.product?.name ?? '', Validators.required],
    brandName: [this.data.product?.brandName ?? '', Validators.required],
    categoryName: [this.data.product?.categoryName ?? '', Validators.required],
    price: [this.data.product?.price ?? 0, [Validators.required, Validators.min(1000)]],
    originalPrice: [this.data.product?.originalPrice ?? null],
    status: [this.data.product?.status ?? 'ACTIVE', Validators.required],
    imageUrl: [this.data.product?.imageUrls?.[0] ?? '', Validators.required],
    description: [this.data.product?.description ?? ''],
    stockAvailable: [this.data.product?.stockAvailable ?? 0, [Validators.min(0)]],
  });

  cancel() {
    this.modalRef.close();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const { imageUrl, stockAvailable, ...rest } = this.form.getRawValue();
    const body: Record<string, unknown> = { ...rest, imageUrls: [imageUrl] };
    if (!this.isEdit) body['stockAvailable'] = stockAvailable;

    const request$ = this.isEdit
      ? this.http.put(ApiEndpoints.admin.productDetail(this.data.product!.id), body)
      : this.http.post(ApiEndpoints.admin.products, body);

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
