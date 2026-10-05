import { Component, inject, DestroyRef, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Category } from '@core/models/common.model';
import { CategoryFormComponent } from '../components/category-form.component';

@Component({
  selector: 'app-admin-category-list',
  imports: [
    NzTableModule, NzButtonModule, NzIconModule, NzSpinModule, NzPopconfirmModule, NzModalModule,
  ],
  templateUrl: './admin-category-list.component.html',
})
export class AdminCategoryListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);

  categories = signal<Category[]>([]);
  loading = signal(false);

  treeRows = computed<{ category: Category; depth: number }[]>(() => {
    const all = this.categories();
    const rows: { category: Category; depth: number }[] = [];
    const addChildren = (parentId: number | null, depth: number) => {
      for (const c of all.filter((c) => c.parentId === parentId)) {
        rows.push({ category: c, depth });
        addChildren(c.id, depth + 1);
      }
    };
    addChildren(null, 0);
    return rows;
  });

  constructor() {
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    this.http.get<{ content: Category[] }>(ApiEndpoints.admin.categories)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.categories.set(res.content);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  openCreateModal() {
    this.openForm(null);
  }

  openEditModal(category: Category) {
    this.openForm(category);
  }

  private openForm(category: Category | null) {
    const modalRef = this.modal.create({
      nzTitle: category ? 'Sửa danh mục' : 'Thêm danh mục mới',
      nzContent: CategoryFormComponent,
      nzData: { category, allCategories: this.categories() },
      nzFooter: null,
      nzWidth: 480,
    });
    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success(category ? 'Cập nhật danh mục thành công' : 'Thêm danh mục thành công');
        this.loadData();
      }
    });
  }

  deleteCategory(id: number) {
    this.http.delete(ApiEndpoints.admin.categoryDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Xóa danh mục thành công');
          this.loadData();
        },
        error: () => this.message.error('Xóa danh mục thất bại'),
      });
  }
}
