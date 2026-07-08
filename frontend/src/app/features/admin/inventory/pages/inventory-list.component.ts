import { Component, inject, DestroyRef, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { RestockModalComponent } from '../components/restock-modal.component';

interface InventoryItem {
  skuId: number;
  skuName: string;
  imageUrl?: string;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
}

interface InventoryResponse {
  content: InventoryItem[];
  totalElements: number;
  totalPages: number;
}

@Component({
  selector: 'app-inventory-list',
  imports: [
    FormsModule, FallbackImageDirective,
    NzTableModule, NzInputModule, NzButtonModule, NzIconModule, NzSpinModule, NzTagModule, NzCheckboxModule,
  ],
  templateUrl: './inventory-list.component.html',
})
export class InventoryListComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  search = signal('');
  lowStockOnly = signal(false);
  total = signal(0);
  page = 0;
  size = 20;
  pageSizeOptions = [10, 20, 50];

  constructor() {
    this.loadData();
  }

  onSearch() {
    this.page = 0;
    this.loadData();
  }

  onLowStockToggle() {
    this.lowStockOnly.update((v) => !v);
    this.page = 0;
    this.loadData();
  }

  onPageChange(page: number) {
    this.page = page - 1;
    this.loadData();
  }

  onPageSizeChange(size: number) {
    this.size = size;
    this.page = 0;
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams()
      .set('page', this.page)
      .set('size', this.size);
    if (this.search()) params = params.set('q', this.search());
    if (this.lowStockOnly()) params = params.set('lowStock', 'true');

    this.http.get<InventoryResponse>(ApiEndpoints.inventory.base, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.items.set(res.content);
          this.total.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  openRestockModal(item: InventoryItem) {
    const modalRef = this.modal.create({
      nzTitle: `Nhập thêm — ${item.skuName}`,
      nzContent: RestockModalComponent,
      nzData: { skuId: item.skuId, skuName: item.skuName },
      nzFooter: null,
      nzWidth: 480,
      nzOnOk: () => {
        const instance = modalRef.getContentComponent() as RestockModalComponent;
        return instance.submit();
      },
    });

    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success('Nhập kho thành công');
        this.loadData();
      }
    });
  }
}
