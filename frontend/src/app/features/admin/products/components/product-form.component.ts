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
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku } from '@core/models/common.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule, HlmButtonDirective, HlmInputDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);
  private mock = inject(MockDataService);

  readonly product = input<Sku | null>(null);
  readonly saved = output<boolean>();
  readonly cancelled = output<void>();

  categories = this.mock.categories;
  brands = this.mock.brands;
  submitting = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    brandName: ['', Validators.required],
    categoryName: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(1000)]],
    originalPrice: [null as number | null],
    status: ['ACTIVE', Validators.required],
    imageUrl: ['', Validators.required],
    description: [''],
    stockAvailable: [0, [Validators.min(0)]],
  });

  get isEdit(): boolean {
    return !!this.product();
  }

  ngOnInit() {
    const p = this.product();
    if (p) {
      this.form.patchValue({
        name: p.name ?? '',
        brandName: p.brandName ?? '',
        categoryName: p.categoryName ?? '',
        price: p.price ?? 0,
        originalPrice: p.originalPrice ?? null,
        status: p.status ?? 'ACTIVE',
        imageUrl: p.imageUrls?.[0] ?? '',
        description: p.description ?? '',
        stockAvailable: p.stockAvailable ?? 0,
      });
    }
  }

  cancel() {
    this.cancelled.emit();
  }

  submit() {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const { imageUrl, stockAvailable, ...rest } = this.form.getRawValue();
    const body: Record<string, unknown> = { ...rest, imageUrls: [imageUrl] };
    if (!this.isEdit) body['stockAvailable'] = stockAvailable;

    const currentProd = this.product();
    const request$ =
      this.isEdit && currentProd
        ? this.http.put(ApiEndpoints.admin.productDetail(currentProd.id), body)
        : this.http.post(ApiEndpoints.admin.products, body);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success(
          this.isEdit ? 'Cập nhật sản phẩm thành công' : 'Thêm sản phẩm thành công',
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
