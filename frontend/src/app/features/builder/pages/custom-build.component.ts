import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BuilderService } from '../services/builder.service';
import { BuilderSlot, BuilderSelection, BuilderValidation } from '@core/models/common.model';
import { SlotListComponent } from '../components/slot-list.component';
import { BuildSummaryComponent } from '../components/build-summary.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';

@Component({
  selector: 'app-custom-build',
  imports: [CommonModule, SlotListComponent, BuildSummaryComponent, LoadingSpinnerComponent],
  templateUrl: './custom-build.component.html',
  styleUrl: './custom-build.component.css',
})
export class CustomBuildComponent {
  private builderService = inject(BuilderService);

  slots: BuilderSlot[] = [];
  selections = new Map<string, BuilderSelection>();
  validation: BuilderValidation | null = null;
  loading = true;

  constructor() {
    this.loadSlots();
  }

  private loadSlots() {
    this.loading = true;
    this.builderService.getSlots().subscribe(res => {
      this.slots = res.content;
      this.loading = false;
    });
  }

  onSlotSelected(selection: BuilderSelection) {
    this.selections.set(selection.slotType, selection);
    this.revalidate();
  }

  onSlotRemoved(slotType: string) {
    this.selections.delete(slotType);
    this.revalidate();
  }

  private revalidate() {
    const items = Array.from(this.selections.values());
    this.builderService.validate(items).subscribe(v => {
      this.validation = v;
    });
  }

  getSlotSelection(slotType: string): BuilderSelection | undefined {
    return this.selections.get(slotType);
  }

  get hasConflict(): boolean {
    return this.validation ? !this.validation.valid : false;
  }

  get missingRequired(): boolean {
    return this.slots.filter(s => s.isRequired).some(s => !this.selections.has(s.slotType));
  }

  get canAddToCart(): boolean {
    return !this.missingRequired && !this.hasConflict;
  }

  onAddToCart() {}
}
