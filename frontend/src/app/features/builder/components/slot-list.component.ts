import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideAngularModule,
  Cpu,
  Layers,
  Zap,
  HardDrive,
  Monitor,
  Box,
  Fan,
  Keyboard,
  Mouse,
  Headphones,
  Pencil,
  Trash2,
  Plus,
  AlertCircle,
  LucideIconData,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  BuilderSlot,
  BuilderSelection,
  BuilderConflict,
  Sku,
  BuilderSlotType,
} from '@core/models/common.model';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { ProductPickerModalComponent } from './product-picker-modal.component';

@Component({
  selector: 'app-slot-list',
  standalone: true,
  imports: [
    CommonModule,
    FallbackImageDirective,
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    CurrencyFormatPipe,
    ProductPickerModalComponent,
  ],
  templateUrl: './slot-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
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

  readonly PencilIcon = Pencil;
  readonly Trash2Icon = Trash2;
  readonly PlusIcon = Plus;
  readonly AlertCircleIcon = AlertCircle;

  private iconMap: Record<string, LucideIconData> = {
    CPU: Cpu,
    MAINBOARD: Layers,
    RAM: Cpu,
    VGA: Monitor,
    PSU: Zap,
    CASE: Box,
    STORAGE: HardDrive,
    COOLER: Fan,
    MONITOR: Monitor,
    KEYBOARD: Keyboard,
    MOUSE: Mouse,
    HEADPHONE: Headphones,
  };

  getSlotIcon(slotType: string): LucideIconData {
    return this.iconMap[slotType.toUpperCase()] || Cpu;
  }

  getConflict(slotType: string): BuilderConflict | undefined {
    return this.conflicts().find((c) => c.slotType === slotType);
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
        slotType: this.pickerSlotType as BuilderSlotType,
        slotName: this.slots().find((s) => s.slotType === this.pickerSlotType)?.slotName ?? '',
        sku,
      });
      this.closePicker();
    }
  }

  confirmRemove(slotType: string) {
    this.removeSlot.emit(slotType);
  }
}
