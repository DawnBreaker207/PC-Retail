import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { BuilderSlot, BuilderSelection, BuilderConflict, Sku } from '@core/models/common.model';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ProductPickerModalComponent } from './product-picker-modal.component';

@Component({
  selector: 'app-slot-list',
  imports: [
    CommonModule, FallbackImageDirective, NzButtonModule, NzIconModule, NzAlertModule,
    CurrencyFormatPipe, ProductPickerModalComponent,
  ],
  templateUrl: './slot-list.component.html',
  styleUrl: './slot-list.component.css',
})
export class SlotListComponent {
  slots = input<BuilderSlot[]>([]);
  selections = input<Map<string, BuilderSelection>>(new Map());
  conflicts = input<BuilderConflict[]>([]);

  selectSlot = output<BuilderSelection>();
  removeSlot = output<string>();

  showPicker = false;
  pickerSlotType: string | null = null;

  slotIcons: Record<string, string> = {
    CPU: 'tool', Mainboard: 'layout', RAM: 'database', GPU: 'thunderbolt',
    PSU: 'thunderbolt', Case: 'build', Storage: 'folder', Cooler: 'fire',
    Monitor: 'monitor', Keyboard: 'tool', Mouse: 'scan', Headphone: 'audio',
  };

  getConflict(slotType: string): BuilderConflict | undefined {
    return this.conflicts().find(c => c.slotType === slotType);
  }

  getSelection(slotType: string): BuilderSelection | undefined {
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

  onProductPicked(sku: Sku) {
    if (this.pickerSlotType) {
      this.selectSlot.emit({
        slotType: this.pickerSlotType as BuilderSlot['slotType'],
        slotName: this.slots().find(s => s.slotType === this.pickerSlotType)?.slotName ?? '',
        sku,
      });
    }
    this.closePicker();
  }

  confirmRemove(slotType: string) {
    this.removeSlot.emit(slotType);
  }
}
