import { Component, inject, signal, input, output, ChangeDetectionStrategy, DestroyRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LucideAngularModule, RotateCcw, Building2, CreditCard, User, Image, X, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmLabelDirective } from '@shared/ui/label';
import { HlmTextareaDirective } from '@shared/ui/textarea';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective,
} from '@shared/ui/dialog';
import { ToastService } from '@core/services/toast.service';
import { OrderService } from '@core/services/order.service';
import { SubmitReturnPayload } from '@core/models/common.model';

@Component({
  selector: 'app-return-request-modal',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmLabelDirective,
    HlmTextareaDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
  ],
  templateUrl: './return-request-modal.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ReturnRequestModalComponent {
  readonly RotateCcwIcon = RotateCcw;
  readonly BuildingIcon = Building2;
  readonly CardIcon = CreditCard;
  readonly UserIcon = User;
  readonly ImageIcon = Image;
  readonly XIcon = X;
  readonly LoaderIcon = Loader2;

  subOrderId = input.required<number>();
  orderId = input.required<number>();

  closed = output<void>();
  submitted = output<void>();

  private orderService = inject(OrderService);
  private toast = inject(ToastService);
  private destroyRef = inject(DestroyRef);

  reason = '';
  bankName = '';
  bankAccount = '';
  accountHolder = '';
  imageUrlInput = '';
  images: string[] = [];
  submitting = signal(false);

  readonly popularBanks = [
    'Vietcombank',
    'Techcombank',
    'MB Bank',
    'BIDV',
    'VietinBank',
    'ACB',
    'VPBank',
    'TPBank',
  ];

  selectBank(name: string) {
    this.bankName = name;
  }

  addImage() {
    const url = this.imageUrlInput.trim();
    if (!url) return;
    if (this.images.length >= 5) {
      this.toast.error('Tối đa 5 hình ảnh minh chứng');
      return;
    }
    this.images.push(url);
    this.imageUrlInput = '';
  }

  removeImage(index: number) {
    this.images.splice(index, 1);
  }

  onSubmit() {
    if (!this.reason.trim()) {
      this.toast.error('Vui lòng nhập lý do trả hàng / hoàn tiền');
      return;
    }
    if (!this.bankName.trim() || !this.bankAccount.trim() || !this.accountHolder.trim()) {
      this.toast.error('Vui lòng điền đầy đủ thông tin tài khoản ngân hàng để nhận hoàn tiền');
      return;
    }

    const payload: SubmitReturnPayload = {
      reason: this.reason.trim(),
      bankName: this.bankName.trim(),
      bankAccount: this.bankAccount.trim(),
      accountHolder: this.accountHolder.trim().toUpperCase(),
      images: this.images,
    };

    this.submitting.set(true);
    this.orderService
      .requestReturn(this.subOrderId(), payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Gửi yêu cầu đổi trả / hoàn tiền thành công!');
          this.submitted.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          console.error('Submit return failed:', err);
          const msg = err?.error?.message || 'Không thể gửi yêu cầu đổi trả. Vui lòng thử lại sau.';
          this.toast.error(msg);
        },
      });
  }
}
