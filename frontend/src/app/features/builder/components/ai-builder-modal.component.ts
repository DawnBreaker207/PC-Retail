import {
  Component,
  inject,
  signal,
  output,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  Sparkles,
  Cpu,
  X,
  Check,
  Loader2,
  TrendingUp,
  Sliders,
  Flame,
  ShieldAlert,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import {
  AiUseCase,
  AiOptimizeRequest,
  AiComponentSlot,
  AiBuildResult,
} from '@core/models/common.model';

@Component({
  selector: 'app-ai-builder-modal',
  standalone: true,
  imports: [
    FormsModule,
    DecimalPipe,
    LucideAngularModule,
    CurrencyFormatPipe,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTooltipDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
  ],
  templateUrl: './ai-builder-modal.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class AiBuilderModalComponent {
  readonly SparklesIcon = Sparkles;
  readonly CpuIcon = Cpu;
  readonly XIcon = X;
  readonly CheckIcon = Check;
  readonly LoaderIcon = Loader2;
  readonly TrendingUpIcon = TrendingUp;
  readonly SlidersIcon = Sliders;
  readonly FlameIcon = Flame;
  readonly ShieldAlertIcon = ShieldAlert;

  closed = output<void>();
  buildApplied = output<AiComponentSlot[]>();

  private http = inject(HttpClient);
  private toast = inject(ToastService);
  private destroyRef = inject(DestroyRef);

  budget = signal(25000000);
  useCase = signal<AiUseCase>('GAMING');
  preferBrand = signal('ALL');

  loading = signal(false);
  results = signal<AiBuildResult[]>([]);
  selectedIndex = signal(0);

  readonly budgetPresets = [
    { label: '15 Triệu', value: 15000000 },
    { label: '25 Triệu', value: 25000000 },
    { label: '35 Triệu', value: 35000000 },
    { label: '50 Triệu', value: 50000000 },
    { label: '70 Triệu', value: 70000000 },
    { label: '100 Triệu', value: 100000000 },
  ];

  readonly useCases: { value: AiUseCase; title: string; desc: string; icon: string }[] = [
    {
      value: 'GAMING',
      title: 'Gaming & Esport',
      desc: 'Tối ưu card đồ họa GPU, tốc độ khung hình cao (FPS)',
      icon: 'gamepad',
    },
    {
      value: 'WORKSTATION',
      title: 'Đồ họa & Render 3D',
      desc: 'Cân bằng CPU đa nhân, GPU render & RAM dung lượng lớn',
      icon: 'palette',
    },
    {
      value: 'OFFICE',
      title: 'Văn phòng & Đa nhiệm',
      desc: 'Nhanh mượt, SSD tốc độ cao, tiết kiệm điện năng',
      icon: 'briefcase',
    },
    {
      value: 'STUDY',
      title: 'Học tập & Lập trình',
      desc: 'CPU hiệu năng tốt, RAM dồi dào để chạy Docker & IDE',
      icon: 'code',
    },
  ];

  readonly brands = [
    { value: 'ALL', label: 'Tối ưu tự động (P/P tốt nhất)' },
    { value: 'INTEL', label: 'Ưu tiên Intel CPU' },
    { value: 'AMD', label: 'Ưu tiên AMD Ryzen' },
    { value: 'NVIDIA', label: 'Ưu tiên NVIDIA GeForce' },
  ];

  generateBuild() {
    if (this.budget() < 5000000) {
      this.toast.error('Ngân sách tối thiểu từ 5,000,000đ để xây dựng cấu hình');
      return;
    }

    const payload: AiOptimizeRequest = {
      budget: this.budget(),
      useCase: this.useCase(),
      preferBrand: this.preferBrand() === 'ALL' ? undefined : this.preferBrand(),
    };

    this.loading.set(true);
    this.http
      .post<AiBuildResult[]>(ApiEndpoints.ai.optimizeBuild, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (builds) => {
          this.loading.set(false);
          const list = builds ?? [];
          this.results.set(list);
          this.selectedIndex.set(0);
          if (list.length === 0) {
            this.toast.info('Không tìm thấy tổ hợp linh kiện phù hợp với ngân sách và thương hiệu yêu cầu.');
          } else {
            this.toast.success(`AI đã tạo thành công ${list.length} phương án cấu hình tối ưu!`);
          }
        },
        error: (err) => {
          this.loading.set(false);
          console.error('AI Optimize build failed:', err);
          this.toast.error(err?.error?.message || 'Không thể tạo cấu hình AI lúc này. Vui lòng thử lại sau.');
        },
      });
  }

  applyCurrentBuild() {
    const list = this.results();
    const idx = this.selectedIndex();
    if (list.length > 0 && list[idx]) {
      this.buildApplied.emit(list[idx].components);
      this.toast.success('Đã nạp toàn bộ cấu hình AI vào dàn máy!');
      this.closed.emit();
    }
  }
}
