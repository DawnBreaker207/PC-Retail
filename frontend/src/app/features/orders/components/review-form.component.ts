import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NZ_MODAL_DATA } from 'ng-zorro-antd/modal';
import { NzRateModule } from 'ng-zorro-antd/rate';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzUploadChangeParam, NzUploadModule, NzUploadFile } from 'ng-zorro-antd/upload';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FallbackImageDirective } from '@shared/directives/fallback-image.directive';
import { OrderService } from '@core/services/order.service';

interface ReviewFormData {
  orderId: number;
  orderItemId: number;
  productName: string;
  productImage: string;
}

@Component({
  selector: 'app-review-form',
  imports: [
    FormsModule,
    NzRateModule,
    NzInputModule,
    NzUploadModule,
    NzButtonModule,
    NzIconModule,
    FallbackImageDirective,
  ],

  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.css',
})
export class ReviewFormComponent {
  private orderService = inject(OrderService);
  private message = inject(NzMessageService);
  data: ReviewFormData = inject(NZ_MODAL_DATA);

  rating = 0;
  comment = '';
  fileList = signal<NzUploadFile[]>([]);
  isSubmitting = signal(false);

  close?: () => void;

  beforeUpload = (file: NzUploadFile): boolean => {
    if (!file.type?.startsWith('image/')) {
      this.message.error('Chỉ chấp nhận file ảnh');
      return false;
    }
    if ((file.size ?? 0) / 1024 / 1024 >= 5) {
      this.message.error('Ảnh không được quá 5MB');
      return false;
    }
    return false;
  };

  onUploadChange(info: NzUploadChangeParam) {
    this.fileList.set(info.fileList);
  }

  onCancel() {
    this.close?.();
  }

  onSubmit() {
    if (this.rating === 0) return;
    this.isSubmitting.set(true);
    this.orderService
      .createReview({
        orderId: this.data.orderId,
        orderItemId: this.data.orderItemId,
        rating: this.rating,
        comment: this.comment,
        images: this.fileList().map((f) => f.url || f.thumbUrl).filter((v): v is string => !!v),
      })
      .subscribe({
        next: () => {
          this.message.success('Đánh giá thành công');
          this.close?.();
        },
        error: () => {
          this.isSubmitting.set(false);
        },
      });
  }
}
