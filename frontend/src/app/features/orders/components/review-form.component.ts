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

  template: `
    <div class="text-center mb-6">
      <img [src]="data.productImage" loading="lazy" appFallbackImage alt="{{ data.productName }}" class="w-20 h-20 object-contain mx-auto mb-3" />
      <p class="font-medium">{{ data.productName }}</p>
    </div>

    <div class="text-center mb-6">
      <p class="text-sm text-muted dark:text-muted-dark mb-2">Chất lượng sản phẩm</p>
      <nz-rate [(ngModel)]="rating" [nzCount]="5"></nz-rate>
    </div>

    <div class="mb-4">
      <label for="review-comment" class="text-sm font-medium mb-1 block" id="review-comment-label">Nhận xét</label>
      <textarea
        nz-input
        [(ngModel)]="comment"
        id="review-comment"
        placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
        [nzAutosize]="{ minRows: 3, maxRows: 6 }"
      ></textarea>
    </div>

    <div class="mb-6">
      <label for="review-images" class="text-sm font-medium mb-1 block">Hình ảnh</label>
      <nz-upload
        [nzAction]="''"
        [nzBeforeUpload]="beforeUpload"
        (nzChange)="onUploadChange($event)"
        [nzFileList]="fileList()"
        [nzShowButton]="fileList().length < 5"
        [nzAccept]="'image/*'"
        id="review-images"
      >
        <button nz-button>
          <span nz-icon nzType="upload"></span>
          Tải ảnh lên
        </button>
      </nz-upload>
    </div>

    <div class="flex justify-end gap-3">
      <button nz-button type="button" (click)="onCancel()">Hủy</button>
      <button
        nz-button
        nzType="primary"
        type="button"
        [nzLoading]="isSubmitting()"
        [disabled]="rating === 0"
        (click)="onSubmit()"
      >
        Gửi đánh giá
      </button>
    </div>
  `,
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
      .createReview(this.data.orderId, this.data.orderItemId, {
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
