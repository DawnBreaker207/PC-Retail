import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, MapPin, Phone, Clock, ChevronRight } from 'lucide-angular';
import { HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective } from '@shared/ui/breadcrumb';

@Component({
  selector: 'app-showroom',
  imports: [RouterLink, LucideAngularModule, HlmBreadcrumbDirective, HlmBreadcrumbLinkDirective, HlmBreadcrumbCurrentDirective],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './showroom.component.html',
})
export class ShowroomComponent {
  readonly ChevronRight = ChevronRight;
  readonly MapPin = MapPin;
  readonly Phone = Phone;
  readonly Clock = Clock;

  showrooms = [
    {
      name: 'DAWN Hà Đông',
      address: '123 Đường ABC, Quận Hà Đông, Hà Nội',
      phone: '1900 5301',
      hours: '8:30 - 21:00 (T2 - CN)',
    },
    {
      name: 'DAWN Cầu Giấy',
      address: '45 Đường Xuân Thủy, Quận Cầu Giấy, Hà Nội',
      phone: '1900 5301',
      hours: '8:30 - 21:00 (T2 - CN)',
    },
    {
      name: 'DAWN Quận 1',
      address: '88 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
      phone: '1900 5301',
      hours: '8:30 - 21:00 (T2 - CN)',
    },
  ];
}
