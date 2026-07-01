import { Injectable } from '@angular/core';
import { of, Observable, delay } from 'rxjs';
import { Sku, Review, ResponsePage, SpecChip, BlogPost, PromoBanner, HomeSection } from '@core/models/common.model';

interface Category {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
  imageUrl: string;
  icon: string;
}

interface Brand {
  id: number;
  name: string;
  slug: string;
}

interface FlashSaleItem {
  skuId: number;
  name: string;
  slug: string;
  imageUrl: string;
  originalPrice: number;
  salePrice: number;
  remaining: number;
  total: number;
  category: string;
  discountPercent: number;
}

interface FlashSaleCampaign {
  id: number;
  campaignName: string;
  endTime: string;
  items: FlashSaleItem[];
}

@Injectable({ providedIn: 'root' })
export class MockDataService {
  private readonly baseUrl = 'https://placehold.co';

  categories: Category[] = [
    { id: 1, name: 'Laptop', slug: 'laptop', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=L`, icon: 'laptop' },
    { id: 2, name: 'Laptop Gaming', slug: 'laptop-gaming', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=LG`, icon: 'play-circle' },
    { id: 3, name: 'PC GVN', slug: 'pc', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PC`, icon: 'desktop' },
    { id: 4, name: 'Main, CPU, VGA', slug: 'linh-kien', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=CPU`, icon: 'tool' },
    { id: 5, name: 'Case, Nguồn, Tản', slug: 'case-nguon-tan', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PSU`, icon: 'build' },
    { id: 6, name: 'Ổ cứng, RAM, Thẻ nhớ', slug: 'o-cung-ram', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=SSD`, icon: 'database' },
    { id: 7, name: 'Loa, Micro, Webcam', slug: 'loa-micro-webcam', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=LOA`, icon: 'customer-service' },
    { id: 8, name: 'Màn hình', slug: 'man-hinh', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=MH`, icon: 'monitor' },
    { id: 9, name: 'Bàn phím', slug: 'ban-phim', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=BP`, icon: 'keyboard' },
    { id: 10, name: 'Chuột + Lót chuột', slug: 'chuot', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=CH`, icon: 'scan' },
    { id: 11, name: 'Tai Nghe', slug: 'tai-nghe', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=TN`, icon: 'audio' },
    { id: 12, name: 'Ghế - Bàn', slug: 'ghe-ban', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=GB`, icon: 'column-height' },
    { id: 13, name: 'Phần mềm, mạng', slug: 'phan-mem-mang', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PM`, icon: 'cloud' },
    { id: 14, name: 'Handheld, Console', slug: 'handheld-console', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=HC`, icon: 'gamepad' },
    { id: 15, name: 'Phụ kiện (Hub, sạc, cáp)', slug: 'phu-kien', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PK`, icon: 'appstore' },
    { id: 16, name: 'Dịch vụ và thông tin khác', slug: 'dich-vu', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=DV`, icon: 'solution' },
  ];

  brands: Brand[] = [
    { id: 1, name: 'ASUS', slug: 'asus' },
    { id: 2, name: 'MSI', slug: 'msi' },
    { id: 3, name: 'ACER', slug: 'acer' },
    { id: 4, name: 'LENOVO', slug: 'lenovo' },
    { id: 5, name: 'DELL', slug: 'dell' },
    { id: 6, name: 'SAMSUNG', slug: 'samsung' },
    { id: 7, name: 'LG', slug: 'lg' },
    { id: 8, name: 'LOGITECH', slug: 'logitech' },
    { id: 9, name: 'GIGABYTE', slug: 'gigabyte' },
    { id: 10, name: 'INTEL', slug: 'intel' },
    { id: 11, name: 'AMD', slug: 'amd' },
    { id: 12, name: 'NVIDIA', slug: 'nvidia' },
  ];

  private productTemplates: {
    name: string; brand: string; cat: string; price: number; origPrice: number;
    sold: number; rating: number; reviews: number; specChips: SpecChip[];
  }[] = [
    {
      name: 'Laptop gaming Gigabyte A16 CMHH2VN893SH (Core i5-13420H/ RTX 4050/ 16GB/ 512GB/ Win 11)', brand: 'GIGABYTE', cat: 'laptop-gaming',
      price: 28790000, origPrice: 30790000, sold: 342, rating: 4.5, reviews: 128,
      specChips: [
        { label: 'CPU', value: 'i5-13420H', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 4050', icon: 'thunderbolt' },
        { label: 'Màn hình', value: '16" FHD+', icon: 'monitor' },
        { label: 'Hz', value: '165Hz', icon: 'eye' },
      ],
    },
    {
      name: 'Laptop gaming Gigabyte A16 GA63H 3THK3VN893SH (R7-260/ RTX 5050/ 16GB/ 512GB)', brand: 'GIGABYTE', cat: 'laptop-gaming',
      price: 38990000, origPrice: 40990000, sold: 156, rating: 4.3, reviews: 64,
      specChips: [
        { label: 'CPU', value: 'R7-260', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5050', icon: 'thunderbolt' },
        { label: 'Màn hình', value: '16" FullHD+', icon: 'monitor' },
        { label: 'Hz', value: '165Hz', icon: 'eye' },
      ],
    },
    {
      name: 'Laptop gaming ACER Nitro ProPanel ANV15-41-R9M1 (R5-7535HS/ RTX 3050/ 16GB/ 512GB/ 15.6" FHD)', brand: 'ACER', cat: 'laptop-gaming',
      price: 23990000, origPrice: 27990000, sold: 89, rating: 4.0, reviews: 32,
      specChips: [
        { label: 'CPU', value: 'R5-7535HS', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 3050', icon: 'thunderbolt' },
        { label: 'Màn hình', value: '15.6" FHD', icon: 'monitor' },
        { label: 'Hz', value: '180Hz', icon: 'eye' },
      ],
    },
    {
      name: 'Laptop gaming LENOVO Legion Pro 5 16IRX9 (i9-14900HX/ RTX 4070/ 32GB/ 1TB/ 16" 2.5K)', brand: 'LENOVO', cat: 'laptop-gaming',
      price: 37990000, origPrice: 42990000, sold: 201, rating: 4.7, reviews: 203,
      specChips: [
        { label: 'CPU', value: 'i9-14900HX', icon: 'cpu' },
        { label: 'RAM', value: '32GB', icon: 'database' },
        { label: 'Ổ cứng', value: '1TB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 4070', icon: 'thunderbolt' },
        { label: 'Màn hình', value: '16" 2.5K', icon: 'monitor' },
        { label: 'Hz', value: '240Hz', icon: 'eye' },
      ],
    },
    {
      name: 'PC GVN Intel i5-12400F/ VGA RTX 5060 (Main H610/ 16GB/ 512GB)', brand: 'INTEL', cat: 'pc',
      price: 23990000, origPrice: 24520000, sold: 123, rating: 4.6, reviews: 67,
      specChips: [
        { label: 'CPU', value: 'i5-12400F', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5060', icon: 'thunderbolt' },
        { label: 'Main', value: 'H610', icon: 'build' },
      ],
    },
    {
      name: 'PC GVN Intel i7-14700F/ VGA RTX 5060 Ti (B760M/ 16GB/ 512GB)', brand: 'INTEL', cat: 'pc',
      price: 35990000, origPrice: 36320000, sold: 78, rating: 4.8, reviews: 45,
      specChips: [
        { label: 'CPU', value: 'i7-14700F', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5060 Ti', icon: 'thunderbolt' },
        { label: 'Main', value: 'B760M', icon: 'build' },
      ],
    },
    {
      name: 'PC GVN I3-12100F/ VGA RTX 5050 (H610/ 8GB/ 256GB)', brand: 'INTEL', cat: 'pc',
      price: 17990000, origPrice: 19620000, sold: 234, rating: 4.3, reviews: 156,
      specChips: [
        { label: 'CPU', value: 'i3-12100F', icon: 'cpu' },
        { label: 'RAM', value: '8GB', icon: 'database' },
        { label: 'Ổ cứng', value: '256GB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5050', icon: 'thunderbolt' },
        { label: 'Main', value: 'H610', icon: 'build' },
      ],
    },
    {
      name: 'PC GVN AMD R7-7800X3D/ RTX 5060 (DDR5)', brand: 'AMD', cat: 'pc',
      price: 42990000, origPrice: 44220000, sold: 45, rating: 4.9, reviews: 28,
      specChips: [
        { label: 'CPU', value: 'R7-7800X3D', icon: 'cpu' },
        { label: 'RAM', value: '32GB DDR5', icon: 'database' },
        { label: 'Ổ cứng', value: '1TB', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5060', icon: 'thunderbolt' },
      ],
    },
    {
      name: 'ASUS ZenBook 14 OLED (Ultra 7/ 16GB/ 512GB/ 14" 2.8K)', brand: 'ASUS', cat: 'laptop',
      price: 22990000, origPrice: 25990000, sold: 167, rating: 4.6, reviews: 92,
      specChips: [
        { label: 'CPU', value: 'Ultra 7', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'Màn hình', value: '14" 2.8K OLED', icon: 'monitor' },
        { label: 'Trọng lượng', value: '1.2 kg', icon: 'column-height' },
      ],
    },
    {
      name: 'DELL XPS 15 9530 (i7-13700H/ 16GB/ 512GB/ 15.6" OLED)', brand: 'DELL', cat: 'laptop',
      price: 32990000, origPrice: 35990000, sold: 89, rating: 4.4, reviews: 45,
      specChips: [
        { label: 'CPU', value: 'i7-13700H', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'Màn hình', value: '15.6" OLED', icon: 'monitor' },
        { label: 'GPU', value: 'RTX 4060', icon: 'thunderbolt' },
      ],
    },
    {
      name: 'LG Gram 16 2024 (Ultra 7/ 16GB/ 512GB/ 16" WQXGA)', brand: 'LG', cat: 'laptop',
      price: 28990000, origPrice: 31990000, sold: 78, rating: 4.5, reviews: 38,
      specChips: [
        { label: 'CPU', value: 'Ultra 7', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'Màn hình', value: '16" WQXGA', icon: 'monitor' },
        { label: 'Trọng lượng', value: '1.19 kg', icon: 'column-height' },
      ],
    },
    {
      name: 'MSI Modern 15 H (i5-13500H/ 16GB/ 512GB/ 15.6" FHD)', brand: 'MSI', cat: 'laptop',
      price: 15990000, origPrice: 18990000, sold: 234, rating: 4.2, reviews: 156,
      specChips: [
        { label: 'CPU', value: 'i5-13500H', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '512GB', icon: 'folder' },
        { label: 'Màn hình', value: '15.6" FHD', icon: 'monitor' },
      ],
    },
    {
      name: 'NVIDIA RTX 4070 Super Gaming X Trio 12GB', brand: 'MSI', cat: 'linh-kien',
      price: 15990000, origPrice: 18990000, sold: 234, rating: 4.7, reviews: 189,
      specChips: [
        { label: 'Dung lượng', value: '12GB GDDR6X', icon: 'thunderbolt' },
        { label: 'CUDA', value: '7168 Cores', icon: 'build' },
        { label: 'Kết nối', value: 'HDMI 2.1a x3', icon: 'scan' },
        { label: 'Nguồn', value: '700W', icon: 'thunderbolt' },
      ],
    },
    {
      name: 'Intel Core i7-14700K (20 nhân/ 28 luồng/ 5.6GHz)', brand: 'INTEL', cat: 'linh-kien',
      price: 8990000, origPrice: 10990000, sold: 345, rating: 4.5, reviews: 210,
      specChips: [
        { label: 'Nhân/Luồng', value: '20/28', icon: 'cpu' },
        { label: 'Xung nhịp', value: '5.6 GHz', icon: 'thunderbolt' },
        { label: 'TDP', value: '125W', icon: 'build' },
        { label: 'Socket', value: 'LGA1700', icon: 'tool' },
      ],
    },
    {
      name: 'Samsung Odyssey OLED G8 34" (3440x1440/ 175Hz/ 0.03ms)', brand: 'SAMSUNG', cat: 'man-hinh',
      price: 25990000, origPrice: 29990000, sold: 67, rating: 4.6, reviews: 42,
      specChips: [
        { label: 'Kích thước', value: '34"', icon: 'monitor' },
        { label: 'Độ phân giải', value: '3440x1440', icon: 'eye' },
        { label: 'Tần số', value: '175Hz', icon: 'thunderbolt' },
        { label: 'Panel', value: 'OLED', icon: 'build' },
      ],
    },
    {
      name: 'LG UltraGear 27GP850 (27" QHD/ 165Hz/ IPS)', brand: 'LG', cat: 'man-hinh',
      price: 8990000, origPrice: 10990000, sold: 178, rating: 4.4, reviews: 95,
      specChips: [
        { label: 'Kích thước', value: '27"', icon: 'monitor' },
        { label: 'Độ phân giải', value: '2560x1440', icon: 'eye' },
        { label: 'Tần số', value: '165Hz', icon: 'thunderbolt' },
        { label: 'Panel', value: 'IPS', icon: 'build' },
      ],
    },
    {
      name: 'Logitech G Pro X Superlight 2 (Wireless/ 32000DPI/ 60g)', brand: 'LOGITECH', cat: 'phu-kien',
      price: 3290000, origPrice: 3990000, sold: 456, rating: 4.7, reviews: 312,
      specChips: [
        { label: 'Kết nối', value: 'Wireless', icon: 'scan' },
        { label: 'DPI', value: '32000', icon: 'eye' },
        { label: 'Trọng lượng', value: '60g', icon: 'column-height' },
        { label: 'Pin', value: '95h', icon: 'thunderbolt' },
      ],
    },
    {
      name: 'Logitech G915 X Lightspeed (Wireless/ RGB/ Low Profile)', brand: 'LOGITECH', cat: 'phu-kien',
      price: 4990000, origPrice: 5990000, sold: 234, rating: 4.5, reviews: 156,
      specChips: [
        { label: 'Kết nối', value: 'Lightspeed', icon: 'scan' },
        { label: 'Switch', value: 'Low Profile', icon: 'tool' },
        { label: 'Pin', value: '40h', icon: 'thunderbolt' },
      ],
    },
    {
      name: 'Máy bộ ACER Altos P130F7 (i7/ RAM 16GB/ SSD 1TB)', brand: 'ACER', cat: 'pc',
      price: 16990000, origPrice: 17490000, sold: 56, rating: 4.2, reviews: 23,
      specChips: [
        { label: 'CPU', value: 'i7-11700', icon: 'cpu' },
        { label: 'RAM', value: '16GB', icon: 'database' },
        { label: 'Ổ cứng', value: '1TB SSD', icon: 'folder' },
        { label: 'OS', value: 'Win 11 Pro', icon: 'solution' },
      ],
    },
    {
      name: 'PC GVN Intel Ultra 9 285K/ RTX 5090 (Z890/ 64GB/ 1TB)', brand: 'INTEL', cat: 'pc',
      price: 206990000, origPrice: 213720000, sold: 12, rating: 5.0, reviews: 8,
      specChips: [
        { label: 'CPU', value: 'Ultra 9 285K', icon: 'cpu' },
        { label: 'RAM', value: '64GB DDR5', icon: 'database' },
        { label: 'Ổ cứng', value: '1TB NVMe', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5090', icon: 'thunderbolt' },
        { label: 'Main', value: 'Z890', icon: 'build' },
      ],
    },
    {
      name: 'PC GVN x ASUS Extreme R9-9950X3D/ RTX 5090 (X870E/ 64GB/ 1TB)', brand: 'AMD', cat: 'pc',
      price: 265990000, origPrice: 267920000, sold: 8, rating: 5.0, reviews: 5,
      specChips: [
        { label: 'CPU', value: 'R9-9950X3D', icon: 'cpu' },
        { label: 'RAM', value: '64GB DDR5', icon: 'database' },
        { label: 'Ổ cứng', value: '1TB NVMe', icon: 'folder' },
        { label: 'GPU', value: 'RTX 5090', icon: 'thunderbolt' },
        { label: 'Main', value: 'X870E', icon: 'build' },
      ],
    },
    {
      name: 'ASUS ROG Harpe Ace Aim Lab (Wireless/ 36000DPI/ 54g)', brand: 'ASUS', cat: 'phu-kien',
      price: 2790000, origPrice: 0, sold: 167, rating: 4.4, reviews: 89,
      specChips: [
        { label: 'Kết nối', value: 'Wireless', icon: 'scan' },
        { label: 'DPI', value: '36000', icon: 'eye' },
        { label: 'Trọng lượng', value: '54g', icon: 'column-height' },
      ],
    },
  ];

  private specsMap: Record<string, Record<string, string>> = {};

  generateProducts(): Sku[] {
    return this.productTemplates.map((t, i) => ({
      id: i + 1,
      name: t.name,
      slug: t.name.toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '')
        .substring(0, 120),
      price: t.price,
      originalPrice: t.origPrice || undefined,
      imageUrls: [
        `${this.baseUrl}/400x400/1e293b/white?text=${encodeURIComponent(t.name.substring(0, 20))}`,
        `${this.baseUrl}/400x400/e53935/white?text=${encodeURIComponent(t.name.substring(0, 20))}`,
      ],
      brandName: t.brand,
      status: 'ACTIVE',
      soldCount: t.sold,
      rating: t.rating,
      reviewCount: t.reviews,
      specChips: t.specChips,
      specs: t.specChips.reduce((acc, chip) => ({ ...acc, [chip.label]: chip.value }), {}),
      description: `${t.name} — Sản phẩm cao cấp chính hãng. Được kiểm tra kỹ lưỡng trước khi bán. Bảo hành 24 tháng. Hỗ trợ đổi trả trong 30 ngày. Miễn phí giao hàng toàn quốc.`,
      stockAvailable: Math.floor(Math.random() * 50) + 5,
      categoryName: t.cat,
    })) as Sku[];
  }

  getFlashSale(): FlashSaleCampaign {
    return {
      id: 1,
      campaignName: 'FLASH SALE - DEAL GẤP RÚT',
      endTime: new Date(Date.now() + 3 * 3600 * 1000).toISOString(),
      items: [
        { skuId: 13, name: 'NVIDIA RTX 4070 Super 12GB', slug: 'rtx-4070-super', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=RTX+4070`, originalPrice: 18990000, salePrice: 15990000, remaining: 8, total: 20, category: 'Linh kiện', discountPercent: 16 },
        { skuId: 1, name: 'Gigabyte A16 i5-13420H/RTX 4050', slug: 'gigabyte-a16', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=GIGABYTE`, originalPrice: 30790000, salePrice: 28790000, remaining: 5, total: 15, category: 'Laptop', discountPercent: 6 },
        { skuId: 20, name: 'Logitech G Pro X Superlight 2', slug: 'g-pro-x-superlight-2', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=Superlight`, originalPrice: 3990000, salePrice: 3290000, remaining: 12, total: 30, category: 'Phụ kiện', discountPercent: 18 },
        { skuId: 17, name: 'Samsung Odyssey OLED G8 34"', slug: 'odyssey-oled-g8', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=Odyssey`, originalPrice: 29990000, salePrice: 25990000, remaining: 3, total: 10, category: 'Màn hình', discountPercent: 13 },
        { skuId: 15, name: 'Intel Core i7-14700K', slug: 'i7-14700k', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=i7-14700K`, originalPrice: 10990000, salePrice: 8990000, remaining: 15, total: 40, category: 'Linh kiện', discountPercent: 18 },
        { skuId: 5, name: 'DELL XPS 15 9530 i7/16GB/512GB', slug: 'xps-15-9530', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=XPS+15`, originalPrice: 35990000, salePrice: 32990000, remaining: 4, total: 12, category: 'Laptop', discountPercent: 8 },
      ],
    };
  }

  getHomeSections(): HomeSection[] {
    return [
      {
        title: 'PC bán chạy',
        link: '/products?category=pc',
        subNav: [
          { label: 'PC I3', slug: 'pc-i3' }, { label: 'PC I5', slug: 'pc-i5' },
          { label: 'PC I7', slug: 'pc-i7' }, { label: 'PC I9', slug: 'pc-i9' },
          { label: 'PC R3', slug: 'pc-r3' }, { label: 'PC R5', slug: 'pc-r5' },
          { label: 'PC R7', slug: 'pc-r7' }, { label: 'PC R9', slug: 'pc-r9' },
        ],
      },
      {
        title: 'Laptop gaming bán chạy',
        link: '/products?category=laptop-gaming',
        subNav: [
          { label: 'ASUS', slug: 'asus-rog' }, { label: 'ACER', slug: 'acer-predator' },
          { label: 'MSI', slug: 'msi-gaming' }, { label: 'LENOVO', slug: 'lenovo-legion' },
          { label: 'GIGABYTE', slug: 'gigabyte' }, { label: 'DELL', slug: 'dell-gaming' },
        ],
      },
      {
        title: 'Laptop văn phòng bán chạy',
        link: '/products?category=laptop',
        subNav: [
          { label: 'ASUS', slug: 'laptop-asus' }, { label: 'ACER', slug: 'laptop-acer' },
          { label: 'MSI', slug: 'laptop-msi' }, { label: 'DELL', slug: 'laptop-dell' },
          { label: 'HP', slug: 'laptop-hp' }, { label: 'LENOVO', slug: 'laptop-lenovo' },
        ],
      },
    ];
  }

  getHeroSlides() {
    return [
      { tag: 'Laptop Gaming Acer', title: 'ACER NITRO PROPANEL SERIES', subtitle: 'Laptop gaming ACER Nitro ProPanel — hiệu năng vượt trội, màn hình 180Hz, giá cực tốt', ctaLabel: 'Khám phá ngay', ctaLink: '/products?category=laptop-gaming&brand=acer', image: `${this.baseUrl}/1200x400/1e293b/e53935?text=ACER+Nitro+ProPanel` },
      { tag: 'PC GVN', title: 'PC ACER ATLOS — Tặng màn hình', subtitle: 'Sở hữu PC ACER ATLOS chính hãng, nhận ngay màn hình miễn phí', ctaLabel: 'Xem ngay', ctaLink: '/products?category=pc&brand=acer', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=PC+Acer+Atlos` },
      { tag: 'PC BUILD', title: 'BUILD PC INTEL CORE ULTRA 9', subtitle: 'PC Intel thế hệ mới với RTX 5090 — hiệu năng đỉnh cao cho gaming và đồ họa', ctaLabel: 'Build ngay', ctaLink: '/products?category=pc', image: `${this.baseUrl}/1200x400/1e293b/ffffff?text=Build+PC+i9` },
      { tag: 'Màn hình', title: 'MÀN HÌNH MÁY TÍNH THÁNG 5/26', subtitle: 'Màn hình từ 24" đến 34" — giảm đến 40%, nhiều ưu đãi đặc biệt', ctaLabel: 'Xem màn hình', ctaLink: '/products?category=man-hinh', image: `${this.baseUrl}/1200x400/0f172a/2563eb?text=Man+hinh+thang+5` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING ACER PREDATOR', subtitle: 'Acer Predator Helios Neo 16 — RTX 4060, màn hình 2.5K 240Hz', ctaLabel: 'Mua ngay', ctaLink: '/products?category=laptop-gaming&brand=acer', image: `${this.baseUrl}/1200x400/1e293b/e53935?text=Acer+Predator` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING MSI', subtitle: 'Laptop MSI Gaming - Sức mạnh từ Intel Core i9 kết hợp RTX 50 Series', ctaLabel: 'Khám phá', ctaLink: '/products?category=laptop-gaming&brand=msi', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=MSI+Gaming` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING GIGABYTE', subtitle: 'Gigabyte A16 Series — RTX 50 Series, màn hình 165Hz, tản nhiệt WINDFORCE', ctaLabel: 'Xem thêm', ctaLink: '/products?category=laptop-gaming&brand=gigabyte', image: `${this.baseUrl}/1200x400/1e293b/ffffff?text=Gigabyte+Gaming` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING LENOVO LEGION', subtitle: 'Lenovo Legion Pro 5 — Intel Gen 13, RTX 4060, màn hình 2.5K 240Hz', ctaLabel: 'Xem ngay', ctaLink: '/products?category=laptop-gaming&brand=lenovo', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=Lenovo+Legion` },
    ];
  }

  getPromoBanners(): PromoBanner[] {
    return [
      { id: 1, title: 'FLASH SALE - DEAL GẤP RÚT', imageUrl: `${this.baseUrl}/340x200/e53935/white?text=FLASH+SALE`, link: '/products?sort=flash-sale' },
      { id: 2, title: 'Màn hình máy tính tháng 5/26', subtitle: 'Giảm đến 40%', imageUrl: `${this.baseUrl}/340x200/2563eb/white?text=Man+hinh`, link: '/products?category=man-hinh' },
      { id: 3, title: 'Chuột chơi game tháng 5/26', subtitle: 'Giảm đến 30%', imageUrl: `${this.baseUrl}/340x200/d97706/white?text=Chuot+game`, link: '/products?category=phu-kien&brand=logitech' },
      { id: 4, title: 'Build PC GVN i9 tặng Màn OLED', subtitle: 'Giảm đến 10 triệu', imageUrl: `${this.baseUrl}/340x200/b71c1c/white?text=Build+PC+i9`, link: '/products?category=pc' },
    ];
  }

  getPromoCampaigns(): PromoBanner[] {
    return [
      { id: 1, title: 'Thu cũ đổi mới', imageUrl: `${this.baseUrl}/280x180/1e293b/white?text=Thu+cu+doi+moi`, link: '#' },
      { id: 2, title: 'Microsoft Office', subtitle: '365 giá tốt', imageUrl: `${this.baseUrl}/280x180/2563eb/white?text=Office+365`, link: '#' },
      { id: 3, title: 'Màn hình máy tính Tháng 10', subtitle: 'Giảm sốc', imageUrl: `${this.baseUrl}/280x180/e53935/white?text=Man+hinh`, link: '#' },
      { id: 4, title: 'Giảm lên đến 2 triệu đồng', subtitle: 'Khi mua PC', imageUrl: `${this.baseUrl}/280x180/d97706/white?text=Giam+2tr`, link: '#' },
    ];
  }

  getBlogPosts(): BlogPost[] {
    return [
      { id: 1, title: 'RTX 5060 vs RTX 4060: Có nên nâng cấp?', excerpt: 'So sánh hiệu năng RTX 5060 và RTX 4060 trong các tựa game phổ biến...', imageUrl: `${this.baseUrl}/280x180/1e293b/e53935?text=RTX+5060+vs+4060`, slug: 'rtx-5060-vs-4060', date: '28/06/2026', category: 'Đánh giá' },
      { id: 2, title: 'Hướng dẫn build PC Gaming dưới 30 triệu', excerpt: 'Cấu hình PC Gaming tối ưu với ngân sách 30 triệu đồng...', imageUrl: `${this.baseUrl}/280x180/0f172a/2563eb?text=Build+PC+30tr`, slug: 'build-pc-duoi-30tr', date: '27/06/2026', category: 'Hướng dẫn' },
      { id: 3, title: 'Top Laptop Gaming đáng mua nhất 2026', excerpt: 'Danh sách những mẫu laptop gaming tốt nhất ở từng phân khúc...', imageUrl: `${this.baseUrl}/280x180/1e293b/e53935?text=Top+Laptop+Gaming`, slug: 'top-laptop-gaming-2026', date: '25/06/2026', category: 'Danh sách' },
      { id: 4, title: 'Intel Core Ultra 9 285K: Review hiệu năng', excerpt: 'Đánh giá chi tiết về CPU Intel thế hệ mới nhất với kiến trúc Arrow Lake...', imageUrl: `${this.baseUrl}/280x180/0f172a/ffffff?text=Ultra+9+285K`, slug: 'intel-ultra-9-285k-review', date: '22/06/2026', category: 'Đánh giá' },
    ];
  }

  getCategories(): Observable<Category[]> {
    return of(this.categories).pipe(delay(100));
  }

  getBrands(): Observable<Brand[]> {
    return of(this.brands).pipe(delay(100));
  }

  getProducts(params?: Record<string, string>): Observable<ResponsePage<Sku>> {
    let filtered = this.generateProducts();
    if (params) {
      if (params['category']) filtered = filtered.filter(p => p.categoryName === params['category']);
      if (params['brand']) filtered = filtered.filter(p => p.brandName.toLowerCase() === params['brand'].toLowerCase());
      if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
      if (params['minPrice']) filtered = filtered.filter(p => p.price >= Number(params['minPrice']));
      if (params['maxPrice']) filtered = filtered.filter(p => p.price <= Number(params['maxPrice']));
      if (params['sort']) {
        switch (params['sort']) {
          case 'price-asc': filtered.sort((a, b) => a.price - b.price); break;
          case 'price-desc': filtered.sort((a, b) => b.price - a.price); break;
          case 'sold': filtered.sort((a, b) => b.soldCount - a.soldCount); break;
          default: break;
        }
      }
    }
    const page = params?.['page'] ? Number(params['page']) : 0;
    const size = params?.['size'] ? Number(params['size']) : 20;
    const start = page * size;
    const paged = filtered.slice(start, start + size);
    return of({ content: paged, pagination: { pageNumber: page, pageSize: size, totalElements: filtered.length } }).pipe(delay(100));
  }

  getProduct(slug: string): Observable<Sku | null> {
    const products = this.generateProducts();
    const p = products.find(pr => pr.slug === slug) || null;
    return of(p).pipe(delay(100));
  }

  getRelatedProducts(slug: string): Observable<Sku[]> {
    const products = this.generateProducts();
    const idx = products.findIndex(p => p.slug === slug);
    const related = products.filter((_, i) => i !== idx).slice(0, 4);
    return of(related).pipe(delay(100));
  }

  getReviews(): Observable<Review[]> {
    const reviewers = ['Nguyễn Văn A', 'Trần Thị B', 'Lê Văn C', 'Phạm Thị D', 'Hoàng Văn E'];
    const comments = ['Sản phẩm rất tốt, đóng gói cẩn thận', 'Chất lượng tuyệt vời, giá hợp lý', 'Hàng chính hãng, giao nhanh', 'Máy chạy mượt, nhiệt độ thấp', 'Màn hình đẹp, màu sắc trung thực'];
    const reviews: Review[] = Array.from({ length: 5 }, (_, i) => ({
      id: i + 1,
      userId: i + 1,
      fullName: reviewers[i],
      rating: Math.floor(Math.random() * 2) + 4,
      comment: comments[i],
      createdAt: new Date(Date.now() - Math.random() * 30 * 86400000).toISOString(),
    }));
    return of(reviews).pipe(delay(100));
  }

  getProductsByCategory(category: string, limit = 8): Sku[] {
    return this.generateProducts().filter(p => p.categoryName === category).slice(0, limit);
  }

  getSectionProducts(subNavSlug?: string, limit = 10): Sku[] {
    const all = this.generateProducts();
    if (subNavSlug === 'pc-i3') return all.filter(p => p.name.toLowerCase().includes('i3')).slice(0, limit);
    if (subNavSlug === 'pc-i5') return all.filter(p => p.name.toLowerCase().includes('i5')).slice(0, limit);
    if (subNavSlug === 'pc-i7') return all.filter(p => p.name.toLowerCase().includes('i7')).slice(0, limit);
    if (subNavSlug === 'pc-i9') return all.filter(p => p.name.toLowerCase().includes('i9')).slice(0, limit);
    if (subNavSlug?.startsWith('pc-r')) return all.filter(p => p.name.toLowerCase().includes('r')).slice(0, limit);
    if (subNavSlug?.includes('asus')) return all.filter(p => p.brandName === 'ASUS').slice(0, limit);
    if (subNavSlug?.includes('acer')) return all.filter(p => p.brandName === 'ACER').slice(0, limit);
    if (subNavSlug?.includes('msi')) return all.filter(p => p.brandName === 'MSI').slice(0, limit);
    if (subNavSlug?.includes('lenovo')) return all.filter(p => p.brandName === 'LENOVO').slice(0, limit);
    if (subNavSlug?.includes('gigabyte')) return all.filter(p => p.brandName === 'GIGABYTE').slice(0, limit);
    if (subNavSlug?.includes('dell')) return all.filter(p => p.brandName === 'DELL').slice(0, limit);
    return all.slice(0, limit);
  }

  getOrders(): Observable<{ content: Order[] }> {
    return of({ content: [] }).pipe(delay(100));
  }
}

interface Order {
  id: number; createdAt: string; totalAmount: number; paymentStatus: string;
  subOrders: { id: number; status: string; fulfillmentType: string; subtotal: number; shippingFee: number; discountAmount: number; finalAmount: number; items: { skuId: number; name: string; imageUrl: string; quantity: number; unitPrice: number; id: number }[] }[];
}
