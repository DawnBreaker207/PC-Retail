import { Component, inject, DestroyRef, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { FormsModule } from '@angular/forms';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { MockDataService } from '@core/services/mock-data.service';
import { Sku } from '@core/models/common.model';
import { ProductFormComponent } from '../components/product-form.component';

interface SkuResponse {
  content: Sku[];
  totalElements: number;
}

@Component({
  selector: 'app-admin-product-list',
  imports: [
    FormsModule, NzTableModule, NzButtonModule, NzIconModule, NzSpinModule, NzTagModule, NzPopconfirmModule,
    NzInputModule, NzSelectModule, NzModalModule, CurrencyFormatPipe, FallbackImageDirective,
  ],
  templateUrl: './admin-product-list.component.html',
})
export class AdminProductListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);
  private mock = inject(MockDataService);

  categories = this.mock.categories;
  brands = this.mock.brands;

  products = signal<Sku[]>([]);
  loading = signal(false);
  search = signal('');
  categoryFilter = signal('');
  brandFilter = signal('');
  statusFilter = signal('');
  total = signal(0);
  page = 1;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  constructor() {
    this.loadData();
  }

  onSearch() {
    this.page = 1;
    this.loadData();
  }

  onCategoryChange(v: string) {
    this.categoryFilter.set(v);
    this.page = 1;
    this.loadData();
  }

  onBrandChange(v: string) {
    this.brandFilter.set(v);
    this.page = 1;
    this.loadData();
  }

  onStatusChange(v: string) {
    this.statusFilter.set(v);
    this.page = 1;
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams().set('page', this.page - 1).set('size', this.size);
    if (this.search()) params = params.set('q', this.search());
    if (this.categoryFilter()) params = params.set('category', this.categoryFilter());
    if (this.brandFilter()) params = params.set('brand', this.brandFilter());
    if (this.statusFilter()) params = params.set('status', this.statusFilter());
    this.http.get<SkuResponse>(ApiEndpoints.admin.products, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.products.set(res.content);
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

  openEditModal(sku: Sku) {
    this.openForm(sku);
  }

  private openForm(sku: Sku | null) {
    const modalRef = this.modal.create({
      nzTitle: sku ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới',
      nzContent: ProductFormComponent,
      nzData: { product: sku },
      nzFooter: null,
      nzWidth: 600,
    });
    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success(sku ? 'Cập nhật sản phẩm thành công' : 'Thêm sản phẩm thành công');
        this.loadData();
      }
    });
  }

  deleteProduct(id: number) {
    this.http.delete(ApiEndpoints.admin.productDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Xóa sản phẩm thành công');
          this.loadData();
        },
        error: () => this.message.error('Xóa sản phẩm thất bại'),
      });
  }
}
