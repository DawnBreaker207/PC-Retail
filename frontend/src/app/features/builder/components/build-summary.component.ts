import { Component, input, output, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  LucideAngularModule,
  ShoppingCart,
  AlertTriangle,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Printer,
  Copy,
  Zap,
  RotateCcw,
  Share2,
  ShieldAlert,
  ShieldCheck,
  Info,
  ChevronDown,
  ChevronRight,
  Cpu,
  Layers,
  HardDrive
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { ToastService } from '@core/services/toast.service';
import { CartStore } from '@features/cart/store/cart.store';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { BuilderSlot, BuilderSelection, BuilderValidation } from '@core/models/common.model';

export interface CompatibilityRuleResult {
  level: 'SAFE' | 'WARNING' | 'BLOCK';
  title: string;
  description: string;
  ruleCode: string;
}

@Component({
  selector: 'app-build-summary',
  standalone: true,
  imports: [
    CommonModule,
    FallbackImageDirective,
    LucideAngularModule,
    HlmButtonDirective,
    CurrencyFormatPipe,
  ],
  templateUrl: './build-summary.component.html',
  styleUrl: './build-summary.component.css',
})
export class BuildSummaryComponent {
  private router = inject(Router);
  private toast = inject(ToastService);
  private cartStore = inject(CartStore);

  readonly CartIcon = ShoppingCart;
  readonly AlertTriangleIcon = AlertTriangle;
  readonly AlertCircleIcon = AlertCircle;
  readonly CheckCircleIcon = CheckCircle2;
  readonly LoaderIcon = Loader2;
  readonly PrinterIcon = Printer;
  readonly CopyIcon = Copy;
  readonly ZapIcon = Zap;
  readonly RotateCcwIcon = RotateCcw;
  readonly ShareIcon = Share2;
  readonly ShieldAlertIcon = ShieldAlert;
  readonly ShieldCheckIcon = ShieldCheck;
  readonly InfoIcon = Info;
  readonly ChevronDownIcon = ChevronDown;
  readonly ChevronRightIcon = ChevronRight;

  selections = input<Map<string, BuilderSelection>>(new Map());
  slots = input<BuilderSlot[]>([]);
  validation = input<BuilderValidation | null>(null);
  canAddToCart = input(false);
  missingRequired = input(false);

  addToCart = output<void>();
  clearBuild = output<void>();

  showAuditDetails = false;

  get selectedItems(): BuilderSelection[] {
    return Array.from(this.selections().values());
  }

  get totalPrice(): number {
    return this.selectedItems.reduce((sum, s) => sum + s.sku.price, 0);
  }

  get estimatedWattage(): number {
    const items = this.selectedItems;
    if (items.length === 0) return 0;

    let watts = 50; // Mainboard, fans, standby
    for (const item of items) {
      const type = item.slotType?.toUpperCase() || '';
      const name = item.sku.name?.toUpperCase() || '';
      if (type.includes('CPU')) {
        watts += name.includes('I9') || name.includes('R9') || name.includes('9950') ? 170 : 85;
      } else if (type.includes('GPU') || type.includes('VGA')) {
        if (name.includes('4090') || name.includes('5090')) watts += 350;
        else if (name.includes('4080') || name.includes('5080')) watts += 280;
        else if (name.includes('4070') || name.includes('7800')) watts += 200;
        else watts += 150;
      } else if (type.includes('RAM')) {
        watts += 15;
      } else if (type.includes('STORAGE') || type.includes('SSD') || type.includes('HDD')) {
        watts += 10;
      }
    }
    return watts;
  }

  get recommendedPsuWattage(): number {
    if (this.estimatedWattage === 0) return 0;
    const rec = Math.ceil((this.estimatedWattage * 1.35) / 50) * 50;
    return Math.max(450, rec);
  }

  // Extract selected PSU wattage if present
  get selectedPsuWattage(): number | null {
    const psuItem = this.selectedItems.find(i =>
      i.slotType?.toUpperCase().includes('PSU') ||
      i.slotType?.toUpperCase().includes('POWER') ||
      i.slotName?.toUpperCase().includes('NGUỒN')
    );
    if (!psuItem) return null;
    const name = psuItem.sku.name?.toUpperCase() || '';
    const match = name.match(/(\d{3,4})\s*W/);
    return match ? parseInt(match[1], 10) : null;
  }

  // Rules Engine: 3-Tier Verification (SAFE / WARNING / BLOCK) inspired by Nihon Chouzai / YAKUSHIN
  get compatibilityReport(): CompatibilityRuleResult[] {
    const results: CompatibilityRuleResult[] = [];
    const items = this.selectedItems;
    if (items.length === 0) return results;

    const cpuItem = items.find(i => i.slotType?.toUpperCase().includes('CPU'));
    const mbItem = items.find(i => i.slotType?.toUpperCase().includes('MAIN') || i.slotType?.toUpperCase().includes('MOTHERBOARD'));
    const ramItem = items.find(i => i.slotType?.toUpperCase().includes('RAM'));
    const coolerItem = items.find(i => i.slotType?.toUpperCase().includes('COOLER') || i.slotName?.toUpperCase().includes('TẢN'));
    const psuItem = items.find(i => i.slotType?.toUpperCase().includes('PSU') || i.slotName?.toUpperCase().includes('NGUỒN'));
    const storageItem = items.find(i => i.slotType?.toUpperCase().includes('STORAGE') || i.slotType?.toUpperCase().includes('SSD'));

    const cpuName = cpuItem?.sku.name?.toUpperCase() || '';
    const mbName = mbItem?.sku.name?.toUpperCase() || '';
    const ramName = ramItem?.sku.name?.toUpperCase() || '';
    const coolerName = coolerItem?.sku.name?.toUpperCase() || '';

    // Rule 1: Socket Compatibility (BLOCK)
    if (cpuItem && mbItem) {
      const isCpuIntel = cpuName.includes('INTEL') || cpuName.includes('CORE') || cpuName.includes('I3') || cpuName.includes('I5') || cpuName.includes('I7') || cpuName.includes('I9');
      const isCpuAmd = cpuName.includes('AMD') || cpuName.includes('RYZEN');

      const isMbAmd = mbName.includes('B650') || mbName.includes('X670') || mbName.includes('A620') || mbName.includes('AM5') || mbName.includes('B550') || mbName.includes('A520') || mbName.includes('AM4');
      const isMbIntel = mbName.includes('Z790') || mbName.includes('B760') || mbName.includes('H610') || mbName.includes('Z690') || mbName.includes('B660') || mbName.includes('LGA1700');

      if (isCpuIntel && isMbAmd) {
        results.push({
          level: 'BLOCK',
          ruleCode: 'SOCKET_MISMATCH_INTEL_AMD',
          title: 'Xung đột Socket CPU & Bo mạch chủ',
          description: `CPU Intel (${cpuItem.sku.name}) không tương thích vật lý với Bo mạch chủ AMD (${mbItem.sku.name}). Chân cắm hoàn toàn khác biệt!`
        });
      } else if (isCpuAmd && isMbIntel) {
        results.push({
          level: 'BLOCK',
          ruleCode: 'SOCKET_MISMATCH_AMD_INTEL',
          title: 'Xung đột Socket CPU & Bo mạch chủ',
          description: `CPU AMD Ryzen (${cpuItem.sku.name}) không thể lắp đặt trên Bo mạch chủ Intel (${mbItem.sku.name}).`
        });
      }
    }

    // Rule 2: RAM Generation Mismatch (BLOCK)
    if (ramItem && mbItem) {
      const isRamDdr4 = ramName.includes('DDR4');
      const isRamDdr5 = ramName.includes('DDR5');
      const isMbDdr4 = mbName.includes('DDR4');
      const isMbDdr5 = mbName.includes('DDR5');

      if (isRamDdr4 && isMbDdr5) {
        results.push({
          level: 'BLOCK',
          ruleCode: 'RAM_GEN_MISMATCH_DDR4_ON_DDR5',
          title: 'Không tương thích chuẩn khe cắm RAM',
          description: `RAM DDR4 (${ramItem.sku.name}) không thể cắm vào Bo mạch chủ khe cắm DDR5 (${mbItem.sku.name}). Vị trí rãnh notch khác nhau.`
        });
      } else if (isRamDdr5 && isMbDdr4) {
        results.push({
          level: 'BLOCK',
          ruleCode: 'RAM_GEN_MISMATCH_DDR5_ON_DDR4',
          title: 'Không tương thích chuẩn khe cắm RAM',
          description: `RAM DDR5 (${ramItem.sku.name}) không tương thích với Bo mạch chủ chuẩn DDR4 (${mbItem.sku.name}).`
        });
      }
    }

    // Rule 3: PSU Wattage Verification (BLOCK or WARNING)
    const psuWatt = this.selectedPsuWattage;
    if (psuItem && psuWatt) {
      if (psuWatt < this.estimatedWattage) {
        results.push({
          level: 'BLOCK',
          ruleCode: 'PSU_INSUFFICIENT_POWER',
          title: 'Công suất nguồn không đủ tải',
          description: `Nguồn ${psuWatt}W thấp hơn công suất tiêu thụ ước tính (${this.estimatedWattage}W). Nguy cơ sập nguồn tức thì khi chơi game hoặc render tải nặng!`
        });
      } else if (psuWatt < this.recommendedPsuWattage) {
        results.push({
          level: 'WARNING',
          ruleCode: 'PSU_MARGIN_TIGHT',
          title: 'Công suất nguồn sát ngưỡng khuyến nghị',
          description: `Nguồn ${psuWatt}W đủ chạy cơ bản, nhưng thấp hơn mức khuyến nghị (${this.recommendedPsuWattage}W). Nên nâng thêm 50-100W để chịu được xung điện đỉnh (transient spikes) của card đồ họa.`
        });
      }
    }

    // Rule 4: High-TDP CPU Cooler Warning (WARNING)
    if (cpuItem) {
      const isHighTdpCpu = cpuName.includes('I9') || cpuName.includes('14900') || cpuName.includes('13900') || cpuName.includes('7950X') || cpuName.includes('7900X');
      if (isHighTdpCpu) {
        const isAioOrDualTower = coolerName.includes('360') || coolerName.includes('240') || coolerName.includes('AIO') || coolerName.includes('KRAKEN') || coolerName.includes('ASSASSIN') || coolerName.includes('D15');
        if (!coolerItem || !isAioOrDualTower) {
          results.push({
            level: 'WARNING',
            ruleCode: 'CPU_HIGH_TDP_COOLER_ADVICE',
            title: 'Khuyến nghị nâng cấp tản nhiệt CPU',
            description: `CPU phân khúc cao cấp tỏa nhiệt rất lớn (~170W-250W). Khuyến nghị trang bị tản nhiệt nước AIO 240/360mm để tránh giảm xung do quá nhiệt (Thermal Throttling).`
          });
        }
      }
    }

    // Rule 5: Storage Type Advice (WARNING)
    if (storageItem) {
      const storageName = storageItem.sku.name?.toUpperCase() || '';
      if (storageName.includes('HDD') && !storageName.includes('SSD') && !storageName.includes('NVME')) {
        results.push({
          level: 'WARNING',
          ruleCode: 'PRIMARY_STORAGE_HDD_WARNING',
          title: 'Khuyến nghị bổ sung ổ cứng SSD',
          description: `Cấu hình chỉ có ổ cứng cơ HDD làm ổ khởi động. Khuyến nghị lắp thêm ít nhất 1 ổ SSD NVMe 250GB-500GB để máy tính đạt tốc độ khởi động Windows tối ưu.`
        });
      }
    }

    // Rule 6: If no BLOCK and no WARNING, emit SAFE status
    const blockCount = results.filter(r => r.level === 'BLOCK').length;
    const warnCount = results.filter(r => r.level === 'WARNING').length;

    if (blockCount === 0 && warnCount === 0 && items.length >= 1) {
      results.push({
        level: 'SAFE',
        ruleCode: 'ALL_RULES_PASSED',
        title: 'Linh kiện đạt chuẩn tương thích',
        description: 'Linh kiện được chọn tương thích với hệ thống và sẵn sàng kết hợp cùng các linh kiện tiếp theo.'
      });
    }

    return results;
  }

  get blockRules(): CompatibilityRuleResult[] {
    return this.compatibilityReport.filter(r => r.level === 'BLOCK');
  }

  get warningRules(): CompatibilityRuleResult[] {
    return this.compatibilityReport.filter(r => r.level === 'WARNING');
  }

  get safeRules(): CompatibilityRuleResult[] {
    return this.compatibilityReport.filter(r => r.level === 'SAFE');
  }

  get hasBlock(): boolean {
    return this.blockRules.length > 0 || (this.validation() != null && !this.validation()!.valid);
  }

  get isValid(): boolean {
    return !this.hasBlock;
  }

  onAddToCart() {
    const items = this.selectedItems;
    if (items.length === 0 || this.hasBlock) return;

    items.forEach((item) => {
      this.cartStore.addItem(item.sku.id, 1);
    });
    this.toast.success('Đã thêm toàn bộ cấu hình vào giỏ hàng!');
    this.router.navigate(['/cart']);
  }

  printBuild() {
    window.print();
  }

  copyBuildText() {
    if (this.selectedItems.length === 0) {
      this.toast.info('Cấu hình đang trống.');
      return;
    }

    const fmt = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
    const lines = [
      '=== CẤU HÌNH PC BUILD TẠI PC-RETAIL ===',
      ...this.selectedItems.map((item, idx) => `${idx + 1}. [${item.slotName}] ${item.sku.name} - ${fmt.format(item.sku.price)}`),
      '----------------------------------------',
      `Ước tính tiêu thụ: ~${this.estimatedWattage}W`,
      `Khuyến nghị nguồn: >= ${this.recommendedPsuWattage}W`,
      `Tổng dự toán: ${fmt.format(this.totalPrice)}`,
      '========================================',
    ];
    navigator.clipboard.writeText(lines.join('\n')).then(() => {
      this.toast.success('Đã sao chép danh sách cấu hình vào clipboard!');
    });
  }

  shareBuild() {
    this.copyBuildText();
  }
}
