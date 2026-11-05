import {
  Component,
  inject,
  DestroyRef,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { BuilderService } from '../services/builder.service';
import { BuilderSlot, BuilderSelection, BuilderValidation } from '@core/models/common.model';
import { SlotListComponent } from '../components/slot-list.component';
import { BuildSummaryComponent } from '../components/build-summary.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner.component';

@Component({
  selector: 'app-custom-build',
  imports: [SlotListComponent, BuildSummaryComponent, LoadingSpinnerComponent],
  templateUrl: './custom-build.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './custom-build.component.css',
})
export class CustomBuildComponent {
  private builderService = inject(BuilderService);
  private destroyRef = inject(DestroyRef);

  slots = signal<BuilderSlot[]>([]);
  selections = signal<Map<string, BuilderSelection>>(new Map());
  validation = signal<BuilderValidation | null>(null);
  loading = signal(true);

  constructor() {
    this.loadSlots();
  }

  private loadSlots() {
    this.loading.set(true);
    this.builderService
      .getSlots()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        this.slots.set(res.content);
        this.loading.set(false);
      });
  }

  onSlotSelected(selection: BuilderSelection) {
    this.selections.update((map) => {
      const next = new Map(map);
      next.set(selection.slotType, selection);
      return next;
    });
    this.revalidate();
  }

  onSlotRemoved(slotType: string) {
    this.selections.update((map) => {
      const next = new Map(map);
      next.delete(slotType);
      return next;
    });
    this.revalidate();
  }

  onClearBuild() {
    this.selections.set(new Map());
    this.revalidate();
  }

  private revalidate() {
    const items = Array.from(this.selections().values());
    this.builderService
      .validate(items)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((v) => {
        this.validation.set(v);
      });
  }

  getSlotSelection(slotType: string): BuilderSelection | undefined {
    return this.selections().get(slotType);
  }

  hasConflict = computed(() => {
    const v = this.validation();
    return v ? !v.valid : false;
  });

  missingRequired = computed(() => {
    return this.slots()
      .filter((s) => s.isRequired)
      .some((s) => !this.selections().has(s.slotType));
  });

  canAddToCart = computed(() => {
    return !this.missingRequired() && !this.hasConflict();
  });

  onAddToCart() {}
}
