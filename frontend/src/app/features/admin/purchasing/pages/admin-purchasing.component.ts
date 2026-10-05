import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzMessageService } from 'ng-zorro-antd/message';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface Supplier {
  id: number;
  name: string;
}

interface PurchaseOrder {
  id: number;
  supplierId: number;
  status: string;
  totalCost: number;
}

@Component({
  selector: 'app-admin-purchasing',
  imports: [FormsModule, NzCardModule, NzTableModule, NzButtonModule, NzInputModule, NzSpinModule],
  template: `
    <nz-card nzTitle="Suppliers">
      <div class="flex gap-2 mb-4">
        <input nz-input [(ngModel)]="supplierName" placeholder="Supplier name" class="max-w-xs" />
        <button nz-button nzType="primary" (click)="addSupplier()" [disabled]="!supplierName.trim()">Add</button>
      </div>
      <nz-table #st [nzData]="suppliers()" [nzFrontPagination]="false" nzSize="small">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
          </tr>
        </thead>
        <tbody>
          @for (s of st.data; track s.id) {
            <tr>
              <td>{{ s.id }}</td>
              <td>{{ s.name }}</td>
            </tr>
          }
        </tbody>
      </nz-table>
    </nz-card>
    <nz-card nzTitle="Purchase orders" class="mt-4">
      <div class="flex gap-2 mb-4">
        <input nz-input [(ngModel)]="newSupplierId" type="number" placeholder="Supplier ID" class="max-w-xs" />
        <button nz-button nzType="primary" (click)="addOrder()" [disabled]="!newSupplierId">New DRAFT order</button>
      </div>
      <nz-spin [nzSpinning]="loading()">
        <nz-table #pt [nzData]="orders()" [nzFrontPagination]="false" nzSize="small">
          <thead>
            <tr>
              <th>ID</th>
              <th>Supplier</th>
              <th>Status</th>
              <th>Total</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (o of pt.data; track o.id) {
              <tr>
                <td>{{ o.id }}</td>
                <td>{{ o.supplierId }}</td>
                <td>{{ o.status }}</td>
                <td>{{ o.totalCost }}</td>
                <td class="flex gap-2">
                  @if (o.status === 'DRAFT') {
                    <button nz-button nzSize="small" (click)="confirm(o.id)">Confirm</button>
                  }
                  @if (o.status === 'CONFIRMED') {
                    <button nz-button nzSize="small" nzType="primary" (click)="receive(o.id)">Receive</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </nz-table>
      </nz-spin>
    </nz-card>
  `,
})
export class AdminPurchasingComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private message = inject(NzMessageService);

  suppliers = signal<Supplier[]>([]);
  orders = signal<PurchaseOrder[]>([]);
  loading = signal(false);
  supplierName = '';
  newSupplierId: number | null = null;

  constructor() {
    this.loadAll();
  }

  loadAll() {
    this.loading.set(true);
    this.http.get<Supplier[]>(ApiEndpoints.purchasing.suppliers).pipe(
      catchError(() => of([] as Supplier[])),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((list) => this.suppliers.set(Array.isArray(list) ? list : []));
    this.http.get<PurchaseOrder[]>(ApiEndpoints.purchasing.orders).pipe(
      catchError(() => of([] as PurchaseOrder[])),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (list) => {
        this.orders.set(Array.isArray(list) ? list : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  addSupplier() {
    const name = this.supplierName.trim();
    if (!name) return;
    this.http.post<Supplier>(ApiEndpoints.purchasing.suppliers, { name }).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((s) => {
      if (s) {
        this.suppliers.update((arr) => [...arr, s]);
        this.supplierName = '';
      } else {
        this.message.error('Add supplier failed');
      }
    });
  }

  addOrder() {
    if (!this.newSupplierId) return;
    this.http.post<PurchaseOrder>(ApiEndpoints.purchasing.orders, { supplierId: this.newSupplierId }).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((o) => {
      if (o) {
        this.orders.update((arr) => [...arr, o]);
        this.newSupplierId = null;
      } else {
        this.message.error('Create order failed');
      }
    });
  }

  confirm(id: number) {
    this.http.post<PurchaseOrder>(ApiEndpoints.purchasing.confirmOrder(id), {}).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((o) => (o ? this.loadAll() : this.message.error('Confirm failed')));
  }

  receive(id: number) {
    this.http.post<PurchaseOrder>(ApiEndpoints.purchasing.receiveOrder(id), {}).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((o) => (o ? this.loadAll() : this.message.error('Receive failed')));
  }
}
