import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';

interface NavItem {
  label: string;
  icon: string;
  link: string;
  children?: { label: string; link: string }[];
}

@Component({
  selector: 'app-sidebar-nav',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  templateUrl: './sidebar-nav.component.html',
  styleUrl: './sidebar-nav.component.css',
})
export class SidebarNavComponent {
  hovered = signal<string | null>(null);

  navItems: NavItem[] = [
    { label: 'Laptop', icon: 'laptop', link: '/products?category=laptop', children: [
      { label: 'Laptop Gaming', link: '/products?category=laptop-gaming' },
      { label: 'Laptop Văn phòng', link: '/products?category=laptop' },
      { label: 'Laptop Đồ họa', link: '/products?category=laptop' },
      { label: 'ASUS', link: '/products?category=laptop-asus' },
      { label: 'ACER', link: '/products?category=laptop-acer' },
      { label: 'MSI', link: '/products?category=laptop-msi' },
      { label: 'LENOVO', link: '/products?category=laptop-lenovo' },
      { label: 'DELL', link: '/products?category=laptop-dell' },
      { label: 'Dưới 15 triệu', link: '/products?category=laptop-duoi-15tr' },
      { label: '15 - 25 triệu', link: '/products?category=laptop-15-25tr' },
      { label: '25 - 40 triệu', link: '/products?category=laptop-25-40tr' },
      { label: 'Trên 40 triệu', link: '/products?category=laptop-tren-40tr' },
    ]},
    { label: 'Laptop Gaming', icon: 'play-circle', link: '/products?category=laptop-gaming', children: [
      { label: 'ASUS ROG', link: '/products?category=rog' },
      { label: 'MSI Gaming', link: '/products?category=msi-gaming' },
      { label: 'ACER Predator', link: '/products?category=predator' },
      { label: 'LENOVO Legion', link: '/products?category=legion' },
      { label: 'Dưới 20tr', link: '/products?category=gaming-duoi-20tr' },
      { label: '20 - 30tr', link: '/products?category=gaming-20-30tr' },
      { label: '30 - 50tr', link: '/products?category=gaming-30-50tr' },
      { label: 'Trên 50tr', link: '/products?category=gaming-tren-50tr' },
    ]},
    { label: 'PC - Máy Tính', icon: 'desktop', link: '/products?category=pc', children: [
      { label: 'PC Gaming', link: '/products?category=pc-gaming' },
      { label: 'PC Workstation', link: '/products?category=pc-workstation' },
      { label: 'PC Văn phòng', link: '/products?category=pc-van-phong' },
      { label: 'PC Đồ họa', link: '/products?category=pc-do-hoa' },
    ]},
    { label: 'Màn Hình', icon: 'monitor', link: '/products?category=man-hinh', children: [
      { label: 'Màn hình Gaming', link: '/products?category=man-hinh' },
      { label: 'Màn hình Văn phòng', link: '/products?category=man-hinh' },
      { label: 'Màn hình 4K', link: '/products?category=man-hinh' },
    ]},
    { label: 'Bàn Phím', icon: 'tool', link: '/products?category=ban-phim' },
    { label: 'Chuột', icon: 'scan', link: '/products?category=chuot' },
    { label: 'Tai Nghe', icon: 'customer-service', link: '/products?category=tai-nghe' },
    { label: 'Linh Kiện', icon: 'tool', link: '/products?category=linh-kien', children: [
      { label: 'CPU', link: '/products?category=linh-kien' },
      { label: 'VGA', link: '/products?category=linh-kien' },
      { label: 'Mainboard', link: '/products?category=linh-kien' },
      { label: 'RAM', link: '/products?category=linh-kien' },
      { label: 'Ổ Cứng SSD/HDD', link: '/products?category=o-cung-ram' },
    ]},
    { label: 'Ổ Cứng, RAM', icon: 'database', link: '/products?category=o-cung-ram' },
    { label: 'Loa, Micro', icon: 'sound', link: '/products?category=loa-micro-webcam' },
    { label: 'Ghế - Bàn', icon: 'rest', link: '/products?category=ghe-ban' },
    { label: 'Phụ Kiện', icon: 'appstore', link: '/products?category=phu-kien', children: [
      { label: 'Hub, Sạc, Cáp', link: '/products?category=phu-kien' },
      { label: 'Lót chuột', link: '/products?category=chuot' },
      { label: 'Webcam', link: '/products?category=loa-micro-webcam' },
    ]},
    { label: 'Handheld, Console', icon: 'play-circle', link: '/products?category=handheld-console' },
    { label: 'Phần Mềm, Mạng', icon: 'wifi', link: '/products?category=phan-mem-mang' },
  ];
}
