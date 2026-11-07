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
import { BuilderSlot, BuilderSelection, BuilderValidation, AiComponentSlot, BuilderSlotType } from '@core/models/common.model';
import { SlotListComponent } from '../components/slot-list.component';
import { BuildSummaryComponent } from '../components/build-summary.component';
import { AiBuilderModalComponent } from '../components/ai-builder-modal.component';
import { HlmSkeletonComponent } from '@shared/ui/skeleton';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import { LucideAngularModule, Sparkles } from 'lucide-angular';

@Component({
  selector: 'app-custom-build',
  imports: [
    SlotListComponent,
    BuildSummaryComponent,
    AiBuilderModalComponent,
    HlmSkeletonComponent,
    HlmBadgeDirective,
    HlmButtonDirective,
    HlmTooltipDirective,
    LucideAngularModule,
  ],
  templateUrl: './custom-build.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './custom-build.component.css',
})
export class CustomBuildComponent {
  readonly SparklesIcon = Sparkles;

  private builderService = inject(BuilderService);
  private destroyRef = inject(DestroyRef);

  slots = signal<BuilderSlot[]>([]);
  selections = signal<Map<string, BuilderSelection>>(new Map());
  validation = signal<BuilderValidation | null>(null);
  loading = signal(true);
  aiModalOpen = signal(false);

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

  onAiBuildApplied(components: AiComponentSlot[]) {
    const newMap = new Map<string, BuilderSelection>();
    for (const c of components) {
      const matchingSlot = this.slots().find((s) => s.slotType === c.slotType) || {
        slotType: c.slotType as BuilderSlotType,
        slotName: c.slotType,
        isRequired: true,
        isRemovable: true,
      };
      newMap.set(c.slotType, {
        slotType: c.slotType as BuilderSlotType,
        slotName: matchingSlot.slotName,
        sku: {
          id: c.skuId,
          name: c.name,
          slug: '',
          price: c.price,
          imageUrls: [],
          brandName: '',
          status: 'ACTIVE',
          soldCount: 0,
          specs: c.specs ? { detail: c.specs } : undefined,
        },
      });
    }
    this.selections.set(newMap);
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
