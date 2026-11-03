import { Component, inject, DestroyRef, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  Plus,
  Search,
  Edit2,
  Trash2,
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
  standalone: true,
  imports: [
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
    CurrencyFormatPipe,
    FallbackImageDirective,
    ProductFormComponent,
  ],
  templateUrl: './admin-product-list.component.html',
})
export class AdminProductListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);
  private mock = inject(MockDataService);

  readonly Plus = Plus;
  readonly Search = Search;
  readonly Edit2 = Edit2;
  readonly Trash2 = Trash2;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly AlertCircle = AlertCircle;
  readonly Loader2 = Loader2;

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

  // Spartan Form Dialog State
  formModalProduct = signal<Sku | null | 'create'>(null);

  // Spartan Delete Confirm Dialog
  deleteTargetId = signal<number | null>(null);

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
          this.products.set(res.content ?? []);
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
    this.formModalProduct.set('create');
  }

  openEditModal(sku: Sku) {
    this.formModalProduct.set(sku);
  }

  closeFormModal() {
    this.formModalProduct.set(null);
  }

  onFormSaved() {
    this.closeFormModal();
    this.loadData();
  }

  confirmDelete(id: number) {
    this.deleteTargetId.set(id);
  }

  cancelDelete() {
    this.deleteTargetId.set(null);
  }

  executeDelete() {
    const id = this.deleteTargetId();
    if (!id) return;
    this.http.delete(ApiEndpoints.admin.productDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Xóa sản phẩm thành công');
          this.cancelDelete();
          this.loadData();
        },
        error: () => this.toast.error('Xóa sản phẩm thất bại'),
      });
  }
}
