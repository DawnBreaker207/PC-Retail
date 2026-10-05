import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';

@Component({
  selector: 'app-faq',
  imports: [RouterLink, NzBreadCrumbModule, NzCollapseModule],
  templateUrl: './faq.component.html',
})
export class FaqComponent {
  faqs = [
    {
      q: 'Tôi có thể thanh toán bằng những hình thức nào?',
      a: 'DAWN hỗ trợ thanh toán khi nhận hàng (COD), chuyển khoản ngân hàng và thanh toán trực tuyến qua VNPay (thẻ ATM nội địa, Visa/Mastercard, ví điện tử).',
    },
    {
      q: 'Đơn hàng của tôi giao trong bao lâu?',
      a: 'Nội thành 1-2 ngày làm việc, các tỉnh thành khác 2-5 ngày làm việc tùy khu vực. Bạn có thể theo dõi trạng thái đơn tại mục "Đơn hàng của tôi".',
    },
    {
      q: 'Sản phẩm lỗi do nhà sản xuất thì xử lý thế nào?',
      a: 'Trong 7 ngày đầu, sản phẩm lỗi do nhà sản xuất được đổi mới 1-1. Sau thời gian này, sản phẩm được bảo hành sửa chữa theo chính sách bảo hành chính hãng.',
    },
    {
      q: 'Tôi có thể build PC theo cấu hình riêng không?',
      a: 'Có, bạn có thể dùng công cụ "Tự Build PC" để tự chọn từng linh kiện, hệ thống sẽ tự kiểm tra tương thích giữa các linh kiện đã chọn.',
    },
    {
      q: 'Làm sao để tra cứu đơn hàng mà không cần đăng nhập?',
      a: 'Bạn có thể vào mục "Tra cứu đơn hàng", nhập mã đơn hàng và số điện thoại đã đặt để xem trạng thái đơn.',
    },
  ];
}
