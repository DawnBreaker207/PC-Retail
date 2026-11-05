import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';
import { ToastService } from '@core/services/toast.service';
import { WarehouseMapComponent } from '../components/warehouse-map.component';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmLabelDirective } from '@shared/ui/label';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import {
  LucideAngularModule,
  Table,
  Warehouse,
  Search,
  PlusCircle,
  History,
  CheckSquare,
  Square,
  X,
  Loader2,
  ChevronLeft,
  ChevronRight,
} from 'lucide-angular';

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

interface InventoryTransaction {
  id: number;
  type: 'IN' | 'OUT';
  quantity: number;
  note: string;
  createdAt: string;
  actorName: string;
}

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    FallbackImageDirective,
    WarehouseMapComponent,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmLabelDirective,
    HlmTextareaDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    LucideAngularModule,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './inventory-list.component.html',
})
export class InventoryListComponent {
  readonly Table = Table;
  readonly Warehouse = Warehouse;
  readonly Search = Search;
  readonly PlusCircle = PlusCircle;
  readonly History = History;
  readonly CheckSquare = CheckSquare;
  readonly Square = Square;
  readonly X = X;
  readonly Loader2 = Loader2;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;

  viewMode = signal<'table' | 'map'>('map');

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);
  private mock = inject(MockDataService);

  brands = this.mock.brands.map((b) => b.name);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  search = '';
  brandFilter = signal('');
  lowStockOnly = signal(false);
  total = signal(0);
  page = 0;
  size = 20;

  // Restock Dialog State (100% Spartan UI)
  restockItem = signal<InventoryItem | null>(null);
  restockQuantity = 1;
  restockNote = '';
  restockSubmitting = signal(false);

  // History Dialog State (100% Spartan UI)
  historyItem = signal<InventoryItem | null>(null);
  historyTransactions = signal<InventoryTransaction[]>([]);
  historyLoading = signal(false);

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

  onBrandChange(brand: string) {
    this.brandFilter.set(brand);
    this.page = 0;
    this.loadData();
  }

  onPageChange(page: number) {
    this.page = page;
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    let params = new HttpParams().set('page', this.page).set('size', this.size);
    if (this.search.trim()) params = params.set('q', this.search.trim());
    if (this.brandFilter()) params = params.set('brand', this.brandFilter());
    if (this.lowStockOnly()) params = params.set('lowStock', 'true');

    this.http
      .get<InventoryResponse>(ApiEndpoints.inventory.base, { params })
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
    this.restockItem.set(item);
    this.restockQuantity = 1;
    this.restockNote = '';
    this.restockSubmitting.set(false);
  }

  closeRestockModal() {
    this.restockItem.set(null);
  }

  submitRestock() {
    const item = this.restockItem();
    const qty = Number(this.restockQuantity);
    if (!item || isNaN(qty) || qty < 1) return;

    this.restockSubmitting.set(true);
    this.http
      .put(ApiEndpoints.inventory.restock(item.skuId), {
        quantity: qty,
        unitCost: 100000,
        note: this.restockNote,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success(`Nhập kho thành công ${qty} sản phẩm.`);
          this.restockSubmitting.set(false);
          this.closeRestockModal();
          this.loadData();
        },
        error: () => {
          this.restockSubmitting.set(false);
          this.toast.error('Nhập kho thất bại, vui lòng thử lại.');
        },
      });
  }

  openHistoryModal(item: InventoryItem) {
    this.historyItem.set(item);
    this.historyLoading.set(true);
    this.historyTransactions.set([]);

    this.http
      .get<{ content: InventoryTransaction[] }>(ApiEndpoints.inventory.transactions(item.skuId))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.historyTransactions.set(res.content || []);
          this.historyLoading.set(false);
        },
        error: () => this.historyLoading.set(false),
      });
  }

  closeHistoryModal() {
    this.historyItem.set(null);
  }
}
