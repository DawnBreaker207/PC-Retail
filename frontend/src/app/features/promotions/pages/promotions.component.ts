import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight, Ticket, Copy, Check } from 'lucide-angular';
import { HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective } from '@shared/ui/breadcrumb';
import { FlashSaleSectionComponent } from '@features/home/components/flash-sale-section.component';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { MockDataService } from '@core/services/mock-data.service';
import { ToastService } from '@core/services/toast.service';
import { HlmButtonDirective } from '@shared/ui/button';

interface VoucherItem {
  code: string;
  discount: string;
  title: string;
  minSpend: string;
  expiry: string;
  tag: string;
}

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, FlashSaleSectionComponent, FallbackImageDirective, HlmButtonDirective, HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './promotions.component.html',
})
export class PromotionsComponent {
  private mock = inject(MockDataService);
  private toast = inject(ToastService);

  readonly ChevronRightIcon = ChevronRight;
  readonly TicketIcon = Ticket;
  readonly CopyIcon = Copy;
  readonly CheckIcon = Check;

  promoBanners = this.mock.getPromoBanners();
  promoCampaigns = this.mock.getPromoCampaigns();
  activeFlashSales = signal<unknown[]>([]);
  copiedCode = signal<string | null>(null);

  vouchers: VoucherItem[] = [
    {
      code: 'DAWN500K',
      discount: '500.000đ',
      title: 'Giảm trực tiếp khi Build PC hoặc mua Laptop Gaming',
      minSpend: 'Đơn từ 15.000.000đ',
      expiry: '31/12/2026',
      tag: 'HOT DEAL',
    },
    {
      code: 'GEARVN300K',
      discount: '300.000đ',
      title: 'Giảm cho Bo mạch chủ, VGA RTX & Màn hình đồ họa',
      minSpend: 'Đơn từ 8.000.000đ',
      expiry: '31/12/2026',
      tag: 'LINH KIỆN',
    },
    {
      code: 'FREESHIP',
      discount: 'MIỄN PHÍ SHIP',
      title: 'Miễn phí giao hàng hỏa tốc 2H toàn quốc',
      minSpend: 'Đơn từ 2.000.000đ',
      expiry: '31/12/2026',
      tag: 'VẬN CHUYỂN',
    },
    {
      code: 'PERIPHERAL15',
      discount: 'GIẢM 15%',
      title: 'Ưu đãi cho Bàn phím cơ, Chuột Gaming & Tai nghe',
      minSpend: 'Tối đa giảm 200.000đ',
      expiry: '31/12/2026',
      tag: 'PHỤ KIỆN',
    },
  ];

  constructor() {
    inject(HttpClient)
      .get<unknown[]>(ApiEndpoints.promotion.flashSalesActive)
      .pipe(catchError(() => of([])))
      .subscribe((list) => this.activeFlashSales.set(Array.isArray(list) ? list : []));
  }

  copyVoucher(code: string): void {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(code).then(() => {
        this.copiedCode.set(code);
        this.toast.success(`Đã sao chép mã voucher ${code}! Dán tại giỏ hàng để nhận giảm giá.`);
        setTimeout(() => {
          if (this.copiedCode() === code) this.copiedCode.set(null);
        }, 3000);
      });
    }
  }
}
