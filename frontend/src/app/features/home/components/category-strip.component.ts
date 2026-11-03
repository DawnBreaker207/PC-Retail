import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  LucideAngularModule,
  Laptop,
  Monitor,
  Cpu,
  Tv,
  Keyboard,
  Mouse,
  Headphones,
  HardDrive,
  ArrowRight,
} from 'lucide-angular';
import { MockDataService } from '@core/services/mock-data.service';

const STRIP_CATEGORIES = [
  {
    label: 'Laptop Gaming',
    sub: 'RTX 40 Series / 240Hz',
    icon: Laptop,
    slug: 'laptop-gaming',
    color: 'bg-red-500/10 text-red-500 border-red-500/20',
  },
  {
    label: 'PC DAWN Custom',
    sub: 'Intel Core Ultra & AMD',
    icon: Monitor,
    slug: 'pc',
    color: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  },
  {
    label: 'Màn Hình Gaming',
    sub: 'OLED / Fast IPS 2K',
    icon: Tv,
    slug: 'man-hinh',
    color: 'bg-cyan-500/10 text-cyan-500 border-cyan-500/20',
  },
  {
    label: 'Linh Kiện Máy Tính',
    sub: 'Main, CPU, VGA khủng',
    icon: Cpu,
    slug: 'linh-kien',
    color: 'bg-purple-500/10 text-purple-500 border-purple-500/20',
  },
  {
    label: 'Bàn Phím Cơ',
    sub: 'Wireless & Hot-swap',
    icon: Keyboard,
    slug: 'ban-phim',
    color: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20',
  },
  {
    label: 'Chuột Gaming',
    sub: 'Siêu nhẹ, Polling 8K',
    icon: Mouse,
    slug: 'chuot',
    color: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
  },
  {
    label: 'Tai Nghe Esports',
    sub: 'Âm thanh vòm 7.1',
    icon: Headphones,
    slug: 'tai-nghe',
    color: 'bg-pink-500/10 text-pink-500 border-pink-500/20',
  },
  {
    label: 'Ổ Cứng & RAM',
    sub: 'NVMe Gen4/Gen5 tốc độ',
    icon: HardDrive,
    slug: 'o-cung-ram',
    color: 'bg-indigo-500/10 text-indigo-500 border-indigo-500/20',
  },
];

@Component({
  selector: 'app-category-strip',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './category-strip.component.html',
  styleUrl: './category-strip.component.css',
})
export class CategoryStripComponent {
  private mock = inject(MockDataService);
  readonly ArrowRightIcon = ArrowRight;

  categories = (() => {
    const products = this.mock.generateProducts();
    return STRIP_CATEGORIES.map((s) => ({
      ...s,
      count: products.filter((p) => p.categoryName === s.slug).length,
    }));
  })();
}
