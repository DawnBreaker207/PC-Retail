import { Component, inject, DestroyRef, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NZ_MODAL_DATA } from 'ng-zorro-antd/modal';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface RestockData {
  skuId: number;
  skuName: string;
}

@Component({
  selector: 'app-restock-modal',
  imports: [FormsModule, NzInputNumberModule, NzInputModule, NzButtonModule],
  template: `
    <div class="py-2">
      <p class="text-sm text-muted mb-4">Nhập số lượng hàng cần nhập thêm cho: <strong>{{ data.skuName }}</strong></p>

      <div class="mb-4">
        <label for="restock-quantity" class="text-sm font-medium mb-1 block">Số lượng nhập</label>
        <nz-input-number id="restock-quantity" [(ngModel)]="quantity" [nzMin]="1" [nzStep]="1" style="width: 100%"></nz-input-number>
      </div>

      <div class="mb-6">
        <label for="restock-note" class="text-sm font-medium mb-1 block">Ghi chú</label>
        <textarea nz-input id="restock-note" [(ngModel)]="note" placeholder="Ghi chú nhập hàng..." [nzAutosize]="{ minRows: 2, maxRows: 4 }"></textarea>
      </div>

      <div class="flex justify-end gap-3">
        <button nz-button nzType="default" type="button" (click)="close?.()">Hủy</button>
        <button nz-button nzType="primary" type="button" [nzLoading]="submitting()" [disabled]="quantity < 1" (click)="submit()">Xác nhận nhập</button>
      </div>
    </div>
  `,
})
export class RestockModalComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);
  data: RestockData = inject(NZ_MODAL_DATA);

  quantity = 1;
  note = '';
  submitting = signal(false);
  close?: () => void;

  submit() {
    if (this.quantity < 1) return;
    this.submitting.set(true);
    this.http.put(ApiEndpoints.inventory.restock(this.data.skuId), {
      quantity: this.quantity,
      note: this.note,
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message.success('Nhập kho thành công');
          this.submitting.set(false);
          this.close?.();
        },
        error: () => {
          this.submitting.set(false);
          this.message.error('Nhập kho thất bại');
        },
      });
  }
}
