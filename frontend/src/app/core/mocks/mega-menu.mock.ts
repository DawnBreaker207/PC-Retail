export interface MegaMenuSubColumn {
  title: string;
  items: { label: string; link: string; queryParams?: Record<string, string>; isHot?: boolean }[];
}

export interface CategoryMegaMenuData {
  categorySlug: string;
  columns: MegaMenuSubColumn[];
  promoBanner?: {
    title: string;
    subtitle: string;
    imageUrl: string;
    link: string;
  };
}

export const CATEGORY_MEGA_MENUS: Record<string, CategoryMegaMenuData> = {
  'laptop': {
    categorySlug: 'laptop',
    columns: [
      {
        title: 'Thương hiệu hàng đầu',
        items: [
          { label: 'Laptop ASUS', link: '/products', queryParams: { category: 'laptop', brand: 'asus' } },
          { label: 'Laptop ACER', link: '/products', queryParams: { category: 'laptop', brand: 'acer' } },
          { label: 'Laptop MSI', link: '/products', queryParams: { category: 'laptop', brand: 'msi' } },
          { label: 'Laptop LENOVO', link: '/products', queryParams: { category: 'laptop', brand: 'lenovo' }, isHot: true },
          { label: 'Laptop DELL', link: '/products', queryParams: { category: 'laptop', brand: 'dell' } },
          { label: 'Laptop HP', link: '/products', queryParams: { category: 'laptop', brand: 'hp' } },
        ],
      },
      {
        title: 'Phân khúc giá',
        items: [
          { label: 'Dưới 15 triệu', link: '/products', queryParams: { category: 'laptop', maxPrice: '15000000' } },
          { label: 'Từ 15 - 20 triệu', link: '/products', queryParams: { category: 'laptop', minPrice: '15000000', maxPrice: '20000000' }, isHot: true },
          { label: 'Từ 20 - 30 triệu', link: '/products', queryParams: { category: 'laptop', minPrice: '20000000', maxPrice: '30000000' } },
          { label: 'Trên 30 triệu cao cấp', link: '/products', queryParams: { category: 'laptop', minPrice: '30000000' } },
        ],
      },
      {
        title: 'Nhu cầu sử dụng',
        items: [
          { label: 'Học tập - Văn phòng', link: '/products', queryParams: { category: 'laptop' } },
          { label: 'Mỏng nhẹ cao cấp', link: '/products', queryParams: { category: 'laptop' }, isHot: true },
          { label: 'Đồ họa - Kỹ thuật', link: '/products', queryParams: { category: 'laptop' } },
          { label: 'Doanh nhân sang trọng', link: '/products', queryParams: { category: 'laptop' } },
        ],
      },
    ],
    promoBanner: {
      title: 'LAPTOP VĂN PHÒNG MỚI',
      subtitle: 'Tặng túi chống sốc cao cấp',
      imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=laptop',
    },
  },
  'laptop-gaming': {
    categorySlug: 'laptop-gaming',
    columns: [
      {
        title: 'Dòng Gaming nổi bật',
        items: [
          { label: 'ASUS ROG / TUF Gaming', link: '/products', queryParams: { category: 'laptop-gaming', brand: 'asus' }, isHot: true },
          { label: 'ACER Predator / Nitro', link: '/products', queryParams: { category: 'laptop-gaming', brand: 'acer' }, isHot: true },
          { label: 'MSI Gaming Series', link: '/products', queryParams: { category: 'laptop-gaming', brand: 'msi' } },
          { label: 'LENOVO Legion / LOQ', link: '/products', queryParams: { category: 'laptop-gaming', brand: 'lenovo' } },
          { label: 'GIGABYTE Aorus / G5', link: '/products', queryParams: { category: 'laptop-gaming', brand: 'gigabyte' } },
        ],
      },
      {
        title: 'Card đồ họa (VGA)',
        items: [
          { label: 'GeForce RTX 4050', link: '/products', queryParams: { category: 'laptop-gaming', q: 'RTX 4050' } },
          { label: 'GeForce RTX 4060', link: '/products', queryParams: { category: 'laptop-gaming', q: 'RTX 4060' }, isHot: true },
          { label: 'GeForce RTX 4070', link: '/products', queryParams: { category: 'laptop-gaming', q: 'RTX 4070' } },
          { label: 'GeForce RTX 4080 / 4090', link: '/products', queryParams: { category: 'laptop-gaming', q: 'RTX 4080' } },
        ],
      },
      {
        title: 'Mức ngân sách',
        items: [
          { label: 'Từ 18 - 25 triệu', link: '/products', queryParams: { category: 'laptop-gaming', minPrice: '18000000', maxPrice: '25000000' } },
          { label: 'Từ 25 - 35 triệu', link: '/products', queryParams: { category: 'laptop-gaming', minPrice: '25000000', maxPrice: '35000000' }, isHot: true },
          { label: 'Trên 35 triệu Hardcore', link: '/products', queryParams: { category: 'laptop-gaming', minPrice: '35000000' } },
        ],
      },
    ],
    promoBanner: {
      title: 'ACER PREDATOR 2026',
      subtitle: 'Màn hình 240Hz, RTX 4070',
      imageUrl: 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=laptop-gaming&brand=acer',
    },
  },
  'pc': {
    categorySlug: 'pc',
    columns: [
      {
        title: 'PC DAWN Theo CPU',
        items: [
          { label: 'PC DAWN Intel Core i3', link: '/products', queryParams: { category: 'pc', q: 'i3' } },
          { label: 'PC DAWN Intel Core i5', link: '/products', queryParams: { category: 'pc', q: 'i5' }, isHot: true },
          { label: 'PC DAWN Intel Core i7', link: '/products', queryParams: { category: 'pc', q: 'i7' } },
          { label: 'PC DAWN Intel Core i9', link: '/products', queryParams: { category: 'pc', q: 'i9' }, isHot: true },
          { label: 'PC DAWN AMD Ryzen 5', link: '/products', queryParams: { category: 'pc', q: 'Ryzen 5' } },
          { label: 'PC DAWN AMD Ryzen 7', link: '/products', queryParams: { category: 'pc', q: 'Ryzen 7' } },
        ],
      },
      {
        title: 'Nhu cầu chiến Game & Đồ họa',
        items: [
          { label: 'PC Gaming Esport (LOL, FO4, Valorant)', link: '/products', queryParams: { category: 'pc' } },
          { label: 'PC AAA Ray Tracing 2K/4K', link: '/products', queryParams: { category: 'pc' }, isHot: true },
          { label: 'PC Thiết kế 2D / Edit Video', link: '/products', queryParams: { category: 'pc' } },
          { label: 'PC Render 3D / Kiến trúc V-Ray', link: '/products', queryParams: { category: 'pc' } },
        ],
      },
      {
        title: 'Tự cấu hình & Dịch vụ',
        items: [
          { label: 'Xây dựng cấu hình PC tự chọn', link: '/pc-builder/custom', isHot: true },
          { label: 'Cấu hình mẫu tối ưu sẵn', link: '/pc-builder/templates' },
          { label: 'Gói bảo hành vàng tận nơi 24/7', link: '/dich-vu' },
        ],
      },
    ],
    promoBanner: {
      title: 'BUILD PC ULTRA 9',
      subtitle: 'Tặng tản nước AIO 360',
      imageUrl: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=500&auto=format&fit=crop&q=80',
      link: '/pc-builder/custom',
    },
  },
  'linh-kien': {
    categorySlug: 'linh-kien',
    columns: [
      {
        title: 'Bộ vi xử lý (CPU)',
        items: [
          { label: 'CPU Intel Core Ultra / Gen 14', link: '/products', queryParams: { category: 'linh-kien', q: 'Intel' }, isHot: true },
          { label: 'CPU AMD Ryzen 9000 / 7000', link: '/products', queryParams: { category: 'linh-kien', q: 'AMD' } },
        ],
      },
      {
        title: 'Bo mạch chủ (Mainboard)',
        items: [
          { label: 'Bo mạch chủ Intel (Z790 / B760)', link: '/products', queryParams: { category: 'linh-kien', q: 'Mainboard' }, isHot: true },
          { label: 'Bo mạch chủ AMD (X670 / B650)', link: '/products', queryParams: { category: 'linh-kien', q: 'Mainboard AMD' } },
        ],
      },
      {
        title: 'Card màn hình (VGA)',
        items: [
          { label: 'GeForce RTX 4060 / 4060 Ti', link: '/products', queryParams: { category: 'linh-kien', q: 'RTX 4060' }, isHot: true },
          { label: 'GeForce RTX 4070 Super / 4080', link: '/products', queryParams: { category: 'linh-kien', q: 'RTX 4070' } },
          { label: 'Radeon RX 7800 XT / 7900 XTX', link: '/products', queryParams: { category: 'linh-kien', q: 'Radeon' } },
        ],
      },
    ],
    promoBanner: {
      title: 'RTX 40 SERIES GIÁ SỐC',
      subtitle: 'Bảo hành chính hãng 36T',
      imageUrl: 'https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=linh-kien',
    },
  },
  'man-hinh': {
    categorySlug: 'man-hinh',
    columns: [
      {
        title: 'Tần số quét (Hz)',
        items: [
          { label: 'Màn hình 100Hz - 144Hz', link: '/products', queryParams: { category: 'man-hinh', q: '144Hz' } },
          { label: 'Màn hình 165Hz - 180Hz', link: '/products', queryParams: { category: 'man-hinh', q: '180Hz' }, isHot: true },
          { label: 'Màn hình 240Hz - 360Hz Esport', link: '/products', queryParams: { category: 'man-hinh', q: '240Hz' }, isHot: true },
        ],
      },
      {
        title: 'Kích thước & Độ phân giải',
        items: [
          { label: 'Màn hình 24 inch FHD', link: '/products', queryParams: { category: 'man-hinh', q: '24' } },
          { label: 'Màn hình 27 inch 2K QHD', link: '/products', queryParams: { category: 'man-hinh', q: '27' }, isHot: true },
          { label: 'Màn hình 32 - 34 inch Cong Ultrawide', link: '/products', queryParams: { category: 'man-hinh', q: '34' } },
        ],
      },
      {
        title: 'Công nghệ tấm nền',
        items: [
          { label: 'Tấm nền Fast IPS chuẩn màu', link: '/products', queryParams: { category: 'man-hinh', q: 'IPS' } },
          { label: 'Tấm nền OLED / QD-OLED đỉnh cao', link: '/products', queryParams: { category: 'man-hinh', q: 'OLED' }, isHot: true },
        ],
      },
    ],
    promoBanner: {
      title: 'MÀN HÌNH OLED 240HZ',
      subtitle: 'Thời gian đáp ứng 0.03ms',
      imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=80',
      link: '/products?category=man-hinh',
    },
  },
  'ban-phim': {
    categorySlug: 'ban-phim',
    columns: [
      {
        title: 'Kiểu kết nối',
        items: [
          { label: 'Bàn phím cơ không dây (Bluetooth / 2.4G)', link: '/products', queryParams: { category: 'ban-phim', q: 'Wireless' }, isHot: true },
          { label: 'Bàn phím cơ có dây Type-C', link: '/products', queryParams: { category: 'ban-phim' } },
        ],
      },
      {
        title: 'Layout & Kích thước',
        items: [
          { label: 'Fullsize 104/108 phím', link: '/products', queryParams: { category: 'ban-phim' } },
          { label: 'Tenkeyless (TKL 87 phím)', link: '/products', queryParams: { category: 'ban-phim' }, isHot: true },
          { label: 'Compact 75% / 65% nhỏ gọn', link: '/products', queryParams: { category: 'ban-phim' } },
        ],
      },
      {
        title: 'Thương hiệu phổ biến',
        items: [
          { label: 'Logitech G', link: '/products', queryParams: { category: 'ban-phim', brand: 'logitech' } },
          { label: 'Akko / MonsGeek', link: '/products', queryParams: { category: 'ban-phim' }, isHot: true },
          { label: 'Corsair / Razer', link: '/products', queryParams: { category: 'ban-phim' } },
        ],
      },
    ],
  },
  'chuot': {
    categorySlug: 'chuot',
    columns: [
      {
        title: 'Loại chuột',
        items: [
          { label: 'Chuột chơi game không dây siêu nhẹ', link: '/products', queryParams: { category: 'chuot', q: 'Wireless' }, isHot: true },
          { label: 'Chuột công thái học văn phòng', link: '/products', queryParams: { category: 'chuot' } },
        ],
      },
      {
        title: 'Thương hiệu hàng đầu',
        items: [
          { label: 'Logitech G Pro Series', link: '/products', queryParams: { category: 'chuot', brand: 'logitech' }, isHot: true },
          { label: 'Razer DeathAdder / Viper', link: '/products', queryParams: { category: 'chuot' } },
          { label: 'Pulsar / Lamzu / Ninjutso', link: '/products', queryParams: { category: 'chuot' }, isHot: true },
        ],
      },
    ],
  },
};
