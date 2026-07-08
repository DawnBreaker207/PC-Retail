import { Component, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { Sku, ResponsePage } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-product-picker-modal',
  imports: [
    CommonModule, FallbackImageDirective, FormsModule, NzModalModule, NzButtonModule, NzInputModule, NzIconModule,
    NzSpinModule, NzGridModule, NzCardModule, NzTagModule, NzEmptyModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './product-picker-modal.component.html',
  styleUrl: './product-picker-modal.component.css',
})
export class ProductPickerModalComponent {
  private http = inject(HttpClient);

  slotType = input.required<string>();
  selected = output<Sku>();
  close = output<void>();

  products = signal<Sku[]>([]);
  loading = signal(true);
  searchQuery = '';

  constructor() {
    this.loadProducts();
  }

  private loadProducts() {
    this.loading.set(true);
    this.http.get<ResponsePage<Sku>>(ApiEndpoints.catalog.skus, {
      params: { slotType: this.slotType(), page: '0', size: '20' },
    }).subscribe(res => {
      this.products.set(res.content);
      this.loading.set(false);
    });
  }

  get filtered() {
    const q = this.searchQuery.toLowerCase();
    if (!q) return this.products();
    return this.products().filter(p => p.name.toLowerCase().includes(q));
  }

  selectProduct(sku: Sku) {
    this.selected.emit(sku);
  }

  onClose() {
    this.close.emit();
  }
}
