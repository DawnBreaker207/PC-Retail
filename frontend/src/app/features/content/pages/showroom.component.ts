import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-showroom',
  imports: [RouterLink, NzBreadCrumbModule, NzIconModule],
  templateUrl: './showroom.component.html',
})
export class ShowroomComponent {
  showrooms = [
    { name: 'DAWN Hà Đông', address: '123 Đường ABC, Quận Hà Đông, Hà Nội', phone: '1900 5301', hours: '8:30 - 21:00 (T2 - CN)' },
    { name: 'DAWN Cầu Giấy', address: '45 Đường Xuân Thủy, Quận Cầu Giấy, Hà Nội', phone: '1900 5301', hours: '8:30 - 21:00 (T2 - CN)' },
    { name: 'DAWN Quận 1', address: '88 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', phone: '1900 5301', hours: '8:30 - 21:00 (T2 - CN)' },
  ];
}
