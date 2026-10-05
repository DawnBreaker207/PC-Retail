import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { NZ_MODAL_DATA } from 'ng-zorro-antd/modal';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface InventoryTransaction {
  id: number;
  type: 'IN' | 'OUT';
  quantity: number;
  note: string;
  createdAt: string;
  actorName: string;
}

interface TransactionHistoryData {
  skuId: number;
  skuName: string;
}

@Component({
  selector: 'app-transaction-history-modal',
  imports: [DatePipe, NzTableModule, NzTagModule, NzSpinModule],
  templateUrl: './transaction-history-modal.component.html',
})
export class TransactionHistoryModalComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  data: TransactionHistoryData = inject(NZ_MODAL_DATA);

  transactions = signal<InventoryTransaction[]>([]);
  loading = signal(true);

  constructor() {
    this.http.get<{ content: InventoryTransaction[] }>(ApiEndpoints.inventory.transactions(this.data.skuId))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.transactions.set(res.content);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
