import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { BuildTemplateDetail, BuilderSelection, BuilderSlot } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { CartStore } from '@features/cart/store/cart.store';
import { ProductPickerModalComponent } from '../components/product-picker-modal.component';

@Component({
  selector: 'app-template-detail',
  imports: [
    CommonModule, FallbackImageDirective, RouterLink, NzButtonModule, NzSpinModule, NzIconModule,
    NzAlertModule, NzBreadCrumbModule, CurrencyFormatPipe,
    ProductPickerModalComponent,
  ],
  templateUrl: './template-detail.component.html',
  styleUrl: './template-detail.component.css',
})
export class TemplateDetailComponent {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private message = inject(NzMessageService);
  private cartStore = inject(CartStore);
  private destroyRef = inject(DestroyRef);

  template = signal<BuildTemplateDetail | null>(null);
  selections = signal<Map<string, BuilderSelection>>(new Map());
  loading = signal(true);

  showPicker = false;
  pickerSlotType: string | null = null;

  slotIcons: Record<string, string> = {
    CPU: 'tool', Mainboard: 'layout', RAM: 'database', GPU: 'thunderbolt',
    PSU: 'thunderbolt', Case: 'build', Storage: 'folder', Cooler: 'fire',
    Monitor: 'monitor', Keyboard: 'tool', Mouse: 'scan', Headphone: 'audio',
  };

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadTemplate(id);
  }

  private loadTemplate(id: number) {
    this.loading.set(true);
    this.http.get<BuildTemplateDetail>(`${ApiEndpoints.builder.templates}/${id}`).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(tpl => {
      this.template.set(tpl);
      const sel = new Map<string, BuilderSelection>();
      tpl.slots.forEach(slot => {
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
      const slotDef = tpl?.slots.find(s => s.slotType === this.pickerSlotType);
      if (!slotDef) return;

      this.selections.update(map => {
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
    const isRequired = this.template()?.slots.find(s => s.slotType === slotType)?.defaultSku;
    if (isRequired) {
      this.message.warning('Không thể bỏ linh kiện bắt buộc');
      return;
    }
    this.selections.update(map => {
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
    return tpl.slots.filter(s => !s.isRemovable).some(s => !this.selections().has(s.slotType));
  }

  onAddToCart() {
    this.selectedItems.forEach(item => {
      this.cartStore.addItem(item.sku.id, 1);
    });
    this.message.success('Đã thêm cấu hình vào giỏ hàng!');
    this.router.navigate(['/cart']);
  }
}
