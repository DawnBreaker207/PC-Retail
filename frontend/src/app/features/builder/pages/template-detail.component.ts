import { Component, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  Cpu,
  Pencil,
  Trash2,
  ShoppingCart,
  ChevronRight,
  AlertTriangle,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmSkeletonComponent } from '@shared/ui/skeleton';
import { HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective } from '@shared/ui/breadcrumb';
import { ToastService } from '@core/services/toast.service';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { BuildTemplateDetail, BuilderSelection, BuilderSlot } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CartStore } from '@features/cart/store/cart.store';
import { ProductPickerModalComponent } from '../components/product-picker-modal.component';

@Component({
  selector: 'app-template-detail',
  standalone: true,
  imports: [
    CommonModule,
    FallbackImageDirective,
    RouterLink,
    LucideAngularModule,
    HlmButtonDirective,
    HlmSkeletonComponent,
    HlmBreadcrumbDirective,
    HlmBreadcrumbLinkDirective,
    HlmBreadcrumbCurrentDirective,
    CurrencyFormatPipe,
    ProductPickerModalComponent,
  ],
  templateUrl: './template-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './template-detail.component.css',
})
export class TemplateDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private toast = inject(ToastService);
  private cartStore = inject(CartStore);
  private destroyRef = inject(DestroyRef);

  readonly CpuIcon = Cpu;
  readonly PencilIcon = Pencil;
  readonly Trash2Icon = Trash2;
  readonly CartIcon = ShoppingCart;
  readonly ChevronRightIcon = ChevronRight;
  readonly AlertTriangleIcon = AlertTriangle;

  template = signal<BuildTemplateDetail | null>(null);
  selections = signal<Map<string, BuilderSelection>>(new Map());
  loading = signal(true);

  showPicker = false;
  pickerSlotType: string | null = null;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadTemplate(id);
  }

  private loadTemplate(id: number) {
    this.loading.set(true);
    this.http
      .get<BuildTemplateDetail>(`${ApiEndpoints.builder.templates}/${id}`)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (tpl) => {
          this.template.set(tpl);
          const sel = new Map<string, BuilderSelection>();
          tpl.slots.forEach((slot) => {
            if (slot.defaultSku) {
              sel.set(slot.slotType, {
                slotType: slot.slotType,
                slotName: slot.slotName,
                sku: slot.defaultSku,
              });
            }
          });
          this.selections.set(sel);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  getSlotSelection(slotType: string): BuilderSelection | undefined {
    return this.selections().get(slotType);
  }

  openPicker(slotType: string) {
    this.pickerSlotType = slotType;
    this.showPicker = true;
  }

  closePicker() {
    this.showPicker = false;
    this.pickerSlotType = null;
  }

  onProductPicked(sku: BuilderSelection['sku']) {
    if (this.pickerSlotType) {
      const tpl = this.template();
      const slotDef = tpl?.slots.find((s) => s.slotType === this.pickerSlotType);
      if (!slotDef) return;

      this.selections.update((map) => {
        map.set(this.pickerSlotType!, {
          slotType: this.pickerSlotType! as BuilderSlot['slotType'],
          slotName: slotDef.slotName,
          sku,
        });
        return new Map(map);
      });
    }
    this.closePicker();
  }

  removeSlot(slotType: string) {
    const isRequired = this.template()?.slots.find((s) => s.slotType === slotType)?.defaultSku;
    if (isRequired) {
      this.toast.warning('Không thể bỏ linh kiện bắt buộc');
      return;
    }
    this.selections.update((map) => {
      map.delete(slotType);
      return new Map(map);
    });
  }

  get selectedItems(): BuilderSelection[] {
    return Array.from(this.selections().values());
  }

  get totalPrice(): number {
    return this.selectedItems.reduce((sum, s) => sum + s.sku.price, 0);
  }

  get missingRequired(): boolean {
    const tpl = this.template();
    if (!tpl) return false;
    return tpl.slots.filter((s) => !s.isRemovable).some((s) => !this.selections().has(s.slotType));
  }

  onAddToCart() {
    this.selectedItems.forEach((item) => {
      this.cartStore.addItem(item.sku.id, 1);
    });
    this.toast.success('Đã thêm toàn bộ cấu hình vào giỏ hàng!');
    this.router.navigate(['/cart']);
  }
}
