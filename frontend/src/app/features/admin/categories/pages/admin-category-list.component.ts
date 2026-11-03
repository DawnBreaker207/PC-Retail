import { Component, inject, DestroyRef, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  Plus,
  Edit2,
  Trash2,
  Folder,
  FolderTree,
  AlertCircle,
  Loader2,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
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
import { Category } from '@core/models/common.model';
import { CategoryFormComponent } from '../components/category-form.component';

@Component({
  selector: 'app-admin-category-list',
  standalone: true,
  imports: [
    LucideAngularModule,
    HlmButtonDirective,
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
    CategoryFormComponent,
  ],
  templateUrl: './admin-category-list.component.html',
})
export class AdminCategoryListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  readonly Plus = Plus;
  readonly Edit2 = Edit2;
  readonly Trash2 = Trash2;
  readonly Folder = Folder;
  readonly FolderTree = FolderTree;
  readonly AlertCircle = AlertCircle;
  readonly Loader2 = Loader2;

  categories = signal<Category[]>([]);
  loading = signal(false);

  // Form Modal State
  formModalCategory = signal<Category | null | 'create'>(null);

  // Delete Confirm Dialog State
  deleteTargetId = signal<number | null>(null);

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
          this.categories.set(res.content ?? []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  openCreateModal() {
    this.formModalCategory.set('create');
  }

  openEditModal(category: Category) {
    this.formModalCategory.set(category);
  }

  closeFormModal() {
    this.formModalCategory.set(null);
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
    this.http.delete(ApiEndpoints.admin.categoryDetail(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Xóa danh mục thành công');
          this.cancelDelete();
          this.loadData();
        },
        error: () => this.toast.error('Xóa danh mục thất bại'),
      });
  }
}
