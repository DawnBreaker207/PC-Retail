import {
  Component,
  inject,
  input,
  output,
  signal,
  OnInit,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Search, X, PackageOpen } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmSkeletonComponent } from '@shared/ui/skeleton';
import {
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
} from '@shared/ui/dialog';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { Sku, ResponsePage } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { MockDataService } from '@core/services/mock-data.service';

@Component({
  selector: 'app-product-picker-modal',
  standalone: true,
  imports: [
    CommonModule,
    FallbackImageDirective,
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmSkeletonComponent,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    CurrencyFormatPipe,
  ],
  templateUrl: './product-picker-modal.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './product-picker-modal.component.css',
})
export class ProductPickerModalComponent implements OnInit {
  private http = inject(HttpClient);
  private mock = inject(MockDataService);

  readonly SearchIcon = Search;
  readonly XIcon = X;
  readonly PackageOpenIcon = PackageOpen;

  slotType = input.required<string>();
  selected = output<Sku>();
  close = output<void>();

  products = signal<Sku[]>([]);
  loading = signal(true);
  searchQuery = '';

  ngOnInit() {
    this.loadProducts();
  }

  private loadProducts() {
    this.loading.set(true);
    const slot = this.slotType();
    this.http
      .get<ResponsePage<Sku>>(ApiEndpoints.catalog.skus, {
        params: { slotType: slot, page: '0', size: '20' },
      })
      .subscribe({
        next: (res) => {
          if (res?.content && res.content.length > 0) {
            this.products.set(res.content);
          } else {
            this.fallbackMock(slot);
          }
          this.loading.set(false);
        },
        error: () => {
          this.fallbackMock(slot);
          this.loading.set(false);
        },
      });
  }

  private fallbackMock(slot: string) {
    const s = slot.toUpperCase();
    this.mock.getProducts({ size: '100' }).subscribe((res) => {
      const allSkus = res.content || [];
      const filtered = allSkus.filter((item) => {
        const n = item.name.toUpperCase();
        if (s.includes('CPU'))
          return (
            n.includes('CORE') || n.includes('RYZEN') || n.includes('INTEL') || n.includes('AMD')
          );
        if (s.includes('MAIN'))
          return (
            n.includes('MAIN') ||
            n.includes('B760') ||
            n.includes('Z790') ||
            n.includes('B650') ||
            n.includes('ASUS')
          );
        if (s.includes('RAM'))
          return (
            n.includes('RAM') || n.includes('DDR4') || n.includes('DDR5') || n.includes('CORSAIR')
          );
        if (s.includes('GPU') || s.includes('VGA'))
          return (
            n.includes('RTX') || n.includes('VGA') || n.includes('GEFORCE') || n.includes('RADEON')
          );
        if (s.includes('PSU'))
          return (
            n.includes('NGUỒN') ||
            n.includes('PSU') ||
            n.includes('850W') ||
            n.includes('750W') ||
            n.includes('650W')
          );
        if (s.includes('STORAGE') || s.includes('SSD'))
          return (
            n.includes('SSD') ||
            n.includes('NVME') ||
            n.includes('SAMSUNG') ||
            n.includes('KINGSTON')
          );
        return true;
      });
      this.products.set(filtered.length > 0 ? filtered : allSkus.slice(0, 10));
    });
  }

  get filtered() {
    const q = this.searchQuery.toLowerCase();
    if (!q) return this.products();
    return this.products().filter((p) => p.name.toLowerCase().includes(q));
  }

  selectProduct(sku: Sku) {
    this.selected.emit(sku);
  }

  onClose() {
    this.close.emit();
  }
}
