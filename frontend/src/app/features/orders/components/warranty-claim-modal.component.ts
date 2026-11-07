import {
  Component,
  inject,
  signal,
  input,
  output,
  OnInit,
  ChangeDetectionStrategy,
  DestroyRef,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import {
  LucideAngularModule,
  ShieldCheck,
  User,
  Phone,
  FileText,
  Image,
  X,
  Loader2,
  Calendar,
} from 'lucide-angular';
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
import { AuthService } from '@core/auth/auth.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { WarrantyItem, SubmitWarrantyClaimRequest, OrderItem } from '@core/models/common.model';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-warranty-claim-modal',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
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
  templateUrl: './warranty-claim-modal.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class WarrantyClaimModalComponent implements OnInit {
  readonly ShieldCheckIcon = ShieldCheck;
  readonly UserIcon = User;
  readonly PhoneIcon = Phone;
  readonly FileTextIcon = FileText;
  readonly ImageIcon = Image;
  readonly XIcon = X;
  readonly LoaderIcon = Loader2;
  readonly CalendarIcon = Calendar;

  warrantyItem = input<WarrantyItem | null>(null);
  subOrderId = input<number | null>(null);
  orderItem = input<OrderItem | null>(null);

  closed = output<void>();
  submitted = output<void>();

  private http = inject(HttpClient);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  selectedItem = signal<WarrantyItem | null>(null);
  availableItems = signal<WarrantyItem[]>([]);
  loadingItems = signal(false);

  customerName = this.auth.currentUser()?.fullName || '';
  customerPhone = this.auth.currentUser()?.phone || '';
  description = '';
  imageUrlInput = '';
  images: string[] = [];
  submitting = signal(false);

  ngOnInit() {
    if (this.warrantyItem()) {
      this.selectedItem.set(this.warrantyItem());
      this.availableItems.set([this.warrantyItem()!]);
    } else if (this.subOrderId()) {
      this.loadWarrantyItems(this.subOrderId()!);
    }
  }

  private loadWarrantyItems(subId: number) {
    this.loadingItems.set(true);
    this.http
      .get<WarrantyItem[]>(ApiEndpoints.warranty.bySubOrder(subId))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.loadingItems.set(false);
          const list = items ?? [];
          this.availableItems.set(list);
          if (list.length > 0) {
            this.selectedItem.set(list[0]);
          }
        },
        error: (err) => {
          this.loadingItems.set(false);
          console.error('Failed to load warranty items', err);
        },
      });
  }

  onItemSelect(event: Event) {
    const val = Number((event.target as HTMLSelectElement).value);
    const found = this.availableItems().find((i) => i.id === val);
    if (found) {
      this.selectedItem.set(found);
    }
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
    const item = this.selectedItem();
    if (!item) {
      this.toast.error('Không tìm thấy thông tin linh kiện bảo hành');
      return;
    }
    if (!this.customerName.trim() || !this.customerPhone.trim()) {
      this.toast.error('Vui lòng nhập họ tên và số điện thoại liên hệ');
      return;
    }
    if (!this.description.trim()) {
      this.toast.error('Vui lòng mô tả chi tiết triệu chứng lỗi của linh kiện');
      return;
    }

    const payload: SubmitWarrantyClaimRequest = {
      customerName: this.customerName.trim(),
      customerPhone: this.customerPhone.trim(),
      description: this.description.trim(),
      images: this.images,
    };

    this.submitting.set(true);
    this.http
      .post(ApiEndpoints.warranty.submitClaim(item.id), payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Gửi yêu cầu bảo hành thành công! Kỹ thuật viên sẽ liên hệ sớm nhất.');
          this.submitted.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          console.error('Submit warranty claim failed:', err);
          this.toast.error(err?.error?.message || 'Gửi yêu cầu bảo hành thất bại. Vui lòng thử lại.');
        },
      });
  }
}
