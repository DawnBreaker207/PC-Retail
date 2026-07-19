import { Pipe, PipeTransform } from '@angular/core';

const STATUS_LABEL: Record<string, string> = {
  PENDING_PAYMENT: 'Chờ thanh toán',
  PAID: 'Đã thanh toán',
  PROCESSING: 'Đang xử lý',
  SHIPPED: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
  EXPIRED: 'Hết hạn',
  RETURN_REQUESTED: 'Yêu cầu trả hàng',
  RETURNED: 'Đã trả hàng',
};

const STATUS_COLOR: Record<string, string> = {
  PENDING_PAYMENT: 'gold',
  PAID: 'blue',
  PROCESSING: 'processing',
  SHIPPED: 'purple',
  DELIVERED: 'success',
  CANCELLED: 'error',
  EXPIRED: 'warning',
  RETURN_REQUESTED: 'warning',
  RETURNED: 'magenta',
};

const FALLBACK_COLOR = '';
const FALLBACK_LABEL = '—';

@Pipe({
  name: 'statusFormat',
  standalone: true,
})
export class StatusFormatPipe implements PipeTransform {
  transform(value: string | null | undefined, type: 'color' | 'label' = 'color'): string {
    const key = value ?? '';
    if (type === 'label') {
      return STATUS_LABEL[key] || key || FALLBACK_LABEL;
    }
    return STATUS_COLOR[key] || FALLBACK_COLOR;
  }
}
