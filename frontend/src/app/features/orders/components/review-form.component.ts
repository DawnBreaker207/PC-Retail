import { Component, inject, signal, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Star, Upload, X, Loader2 } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { ToastService } from '@core/services/toast.service';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { OrderService } from '@core/services/order.service';
import { OrderItem } from '@core/models/common.model';

@Component({
  selector: 'app-review-form',
  imports: [
    FormsModule,
    LucideAngularModule,
    HlmButtonDirective,
    FallbackImageDirective,
  ],
  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.css',
})
export class ReviewFormComponent {
  readonly Star = Star;
  readonly Upload = Upload;
  readonly X = X;
  readonly Loader2 = Loader2;

  orderId = input.required<number>();
  item = input.required<OrderItem>();

  closed = output<void>();
  submitted = output<void>();

  private orderService = inject(OrderService);
  private toast = inject(ToastService);

  rating = signal(5);
  hoverRating = signal(0);
  comment = signal('');
  uploadedImages = signal<string[]>([]);
  isSubmitting = signal(false);

  setRating(val: number) {
    this.rating.set(val);
  }

  setHoverRating(val: number) {
    this.hoverRating.set(val);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        this.toast.error('Chỉ chấp nhận file ảnh');
        continue;
      }
      if (file.size / 1024 / 1024 >= 5) {
        this.toast.error('Ảnh không được vượt quá 5MB');
        continue;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const res = e.target?.result as string;
        if (res && this.uploadedImages().length < 5) {
          this.uploadedImages.update((imgs) => [...imgs, res]);
        }
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(index: number) {
    this.uploadedImages.update((imgs) => imgs.filter((_, i) => i !== index));
  }

  onCancel() {
    this.closed.emit();
  }

  onSubmit() {
    if (this.rating() === 0) {
      this.toast.warning('Vui lòng chọn số sao đánh giá');
      return;
    }
    this.isSubmitting.set(true);
    this.orderService
      .createReview({
        orderId: this.orderId(),
        orderItemId: this.item().id,
        skuId: this.item().skuId || this.item().id,
        rating: this.rating(),
        comment: this.comment(),
        images: this.uploadedImages(),
      })
      .subscribe({
        next: () => {
          this.toast.success('Đánh giá thành công. Cảm ơn phản hồi của bạn!');
          this.submitted.emit();
          this.closed.emit();
        },
        error: () => {
          this.isSubmitting.set(false);
          this.toast.error('Không thể gửi đánh giá. Vui lòng thử lại.');
        },
      });
  }
}

