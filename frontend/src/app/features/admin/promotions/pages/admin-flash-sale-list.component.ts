import {
  Component,
  inject,
  signal,
  OnInit,
  DestroyRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  LucideAngularModule,
  Zap,
  Plus,
  Trash2,
  Calendar,
  Clock,
  Package,
  Loader2,
  AlertCircle,
  Tag,
  CheckCircle,
} from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { HlmTooltipDirective } from '@shared/ui/tooltip';
import {
  HlmTableDirective,
  HlmTableHeaderDirective,
  HlmTableBodyDirective,
  HlmTableRowDirective,
  HlmTableHeadDirective,
  HlmTableCellDirective,
} from '@shared/ui/table';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ToastService } from '@core/services/toast.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { FlashSaleCampaign, FlashSaleItem } from '@core/models/common.model';

@Component({
  selector: 'app-admin-flash-sale-list',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    LucideAngularModule,
    CurrencyFormatPipe,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmTooltipDirective,
    HlmTableDirective,
    HlmTableHeaderDirective,
    HlmTableBodyDirective,
    HlmTableRowDirective,
    HlmTableHeadDirective,
    HlmTableCellDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
  ],
  templateUrl: './admin-flash-sale-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class AdminFlashSaleListComponent implements OnInit {
  readonly ZapIcon = Zap;
  readonly PlusIcon = Plus;
  readonly Trash2Icon = Trash2;
  readonly CalendarIcon = Calendar;
  readonly ClockIcon = Clock;
  readonly PackageIcon = Package;
  readonly LoaderIcon = Loader2;
  readonly AlertCircleIcon = AlertCircle;
  readonly TagIcon = Tag;
  readonly CheckCircleIcon = CheckCircle;

  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  campaigns = signal<FlashSaleCampaign[]>([]);
  selectedCampaign = signal<FlashSaleCampaign | null>(null);
  items = signal<FlashSaleItem[]>([]);

  loadingCampaigns = signal(false);
  loadingItems = signal(false);
  submitting = signal(false);

  // Modal create campaign
  createCampaignModalOpen = signal(false);
  newCampaignName = signal('');
  newCampaignStart = signal('');
  newCampaignEnd = signal('');

  // Modal add item
  addItemModalOpen = signal(false);
  newItemSkuId = signal<number | null>(null);
  newItemFlashPrice = signal<number | null>(null);
  newItemQuantityLimit = signal<number>(50);

  ngOnInit() {
    this.loadCampaigns();
  }

  loadCampaigns() {
    this.loadingCampaigns.set(true);
    this.http
      .get<FlashSaleCampaign[]>(ApiEndpoints.promotion.flashSalesActive)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.loadingCampaigns.set(false);
          const list = res ?? [];
          this.campaigns.set(list);
          if (list.length > 0) {
            if (!this.selectedCampaign() || !list.some((c) => c.id === this.selectedCampaign()?.id)) {
              this.selectCampaign(list[0]);
            } else {
              this.loadItems(this.selectedCampaign()!.id);
            }
          } else {
            this.selectedCampaign.set(null);
            this.items.set([]);
          }
        },
        error: (err) => {
          this.loadingCampaigns.set(false);
          console.error('Load flash sales failed:', err);
          this.toast.error('Không thể tải danh sách chiến dịch Flash Sale');
        },
      });
  }

  selectCampaign(campaign: FlashSaleCampaign) {
    this.selectedCampaign.set(campaign);
    this.loadItems(campaign.id);
  }

  loadItems(campaignId: number) {
    this.loadingItems.set(true);
    this.http
      .get<FlashSaleItem[]>(ApiEndpoints.promotion.adminFlashSaleItems(campaignId))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.loadingItems.set(false);
          this.items.set(items ?? []);
        },
        error: (err) => {
          this.loadingItems.set(false);
          console.error('Load flash sale items failed:', err);
          this.toast.error('Không thể tải danh sách sản phẩm trong chiến dịch');
        },
      });
  }

  openCreateCampaignModal() {
    const now = new Date();
    const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    this.newCampaignName.set('Flash Sale Giờ Vàng ' + now.toLocaleDateString('vi-VN'));
    this.newCampaignStart.set(now.toISOString().slice(0, 16));
    this.newCampaignEnd.set(tomorrow.toISOString().slice(0, 16));
    this.createCampaignModalOpen.set(true);
  }

  submitCreateCampaign() {
    if (!this.newCampaignName().trim()) {
      this.toast.error('Vui lòng nhập tên chiến dịch');
      return;
    }
    if (!this.newCampaignStart() || !this.newCampaignEnd()) {
      this.toast.error('Vui lòng chọn thời gian bắt đầu và kết thúc');
      return;
    }

    const payload = {
      name: this.newCampaignName().trim(),
      startTime: new Date(this.newCampaignStart()).toISOString(),
      endTime: new Date(this.newCampaignEnd()).toISOString(),
    };

    this.submitting.set(true);
    this.http
      .post<FlashSaleCampaign>(ApiEndpoints.promotion.adminFlashSales, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (camp) => {
          this.submitting.set(false);
          this.toast.success(`Chiến dịch "${camp.name}" đã được tạo thành công!`);
          this.createCampaignModalOpen.set(false);
          this.loadCampaigns();
        },
        error: (err) => {
          this.submitting.set(false);
          console.error('Create campaign failed:', err);
          this.toast.error(err?.error?.message || 'Tạo chiến dịch Flash Sale thất bại');
        },
      });
  }

  deleteCampaign(campaign: FlashSaleCampaign) {
    if (!confirm(`Bạn có chắc muốn xóa chiến dịch "${campaign.name}"?`)) return;

    this.http
      .delete(ApiEndpoints.promotion.adminFlashSaleCampaign(campaign.id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Đã xóa chiến dịch Flash Sale');
          if (this.selectedCampaign()?.id === campaign.id) {
            this.selectedCampaign.set(null);
          }
          this.loadCampaigns();
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Xóa chiến dịch thất bại');
        },
      });
  }

  openAddItemModal() {
    this.newItemSkuId.set(null);
    this.newItemFlashPrice.set(null);
    this.newItemQuantityLimit.set(50);
    this.addItemModalOpen.set(true);
  }

  submitAddItem() {
    const campaign = this.selectedCampaign();
    if (!campaign) return;

    const skuId = this.newItemSkuId();
    const flashPrice = this.newItemFlashPrice();
    const quantityLimit = this.newItemQuantityLimit();

    if (!skuId || skuId <= 0) {
      this.toast.error('Vui lòng nhập mã SKU ID hợp lệ');
      return;
    }
    if (!flashPrice || flashPrice <= 0) {
      this.toast.error('Vui lòng nhập giá Flash Sale hợp lệ');
      return;
    }
    if (!quantityLimit || quantityLimit <= 0) {
      this.toast.error('Vui lòng nhập số lượng giới hạn');
      return;
    }

    const payload = { skuId, flashPrice, quantityLimit };

    this.submitting.set(true);
    this.http
      .post<FlashSaleItem>(ApiEndpoints.promotion.adminFlashSaleItems(campaign.id), payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Đã thêm sản phẩm vào chiến dịch Flash Sale!');
          this.addItemModalOpen.set(false);
          this.loadItems(campaign.id);
        },
        error: (err) => {
          this.submitting.set(false);
          console.error('Add flash sale item failed:', err);
          this.toast.error(err?.error?.message || 'Thêm sản phẩm thất bại');
        },
      });
  }

  deleteItem(item: FlashSaleItem) {
    if (!confirm(`Bạn có chắc muốn loại bỏ SKU #${item.skuId} khỏi chiến dịch?`)) return;

    this.http
      .delete(ApiEndpoints.promotion.adminFlashSaleItem(item.id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Đã xóa sản phẩm khỏi chiến dịch Flash Sale');
          if (this.selectedCampaign()) {
            this.loadItems(this.selectedCampaign()!.id);
          }
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Xóa sản phẩm thất bại');
        },
      });
  }
}
