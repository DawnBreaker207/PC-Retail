export interface ProductTemplate {
  name: string; brand: string; cat: string; price: number; origPrice: number;
  sold: number; rating: number; reviews: number;
  specChips: { label: string; value: string; icon: string }[];
}

export const PRODUCT_TEMPLATES: ProductTemplate[] = [
  {
    name: 'Laptop gaming Gigabyte A16 CMHH2VN893SH (Core i5-13420H/ RTX 4050/ 16GB/ 512GB/ Win 11)', brand: 'GIGABYTE', cat: 'laptop-gaming',
    price: 28790000, origPrice: 30790000, sold: 342, rating: 4.5, reviews: 128,
    specChips: [
      { label: 'CPU', value: 'i5-13420H', icon: 'tool' },
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
      { label: 'CPU', value: 'R7-260', icon: 'tool' },
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
      { label: 'CPU', value: 'R5-7535HS', icon: 'tool' },
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
      { label: 'CPU', value: 'i9-14900HX', icon: 'tool' },
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
      { label: 'CPU', value: 'i5-12400F', icon: 'tool' },
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
      { label: 'CPU', value: 'i7-14700F', icon: 'tool' },
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
      { label: 'CPU', value: 'i3-12100F', icon: 'tool' },
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
      { label: 'CPU', value: 'R7-7800X3D', icon: 'tool' },
      { label: 'RAM', value: '32GB DDR5', icon: 'database' },
      { label: 'Ổ cứng', value: '1TB', icon: 'folder' },
      { label: 'GPU', value: 'RTX 5060', icon: 'thunderbolt' },
    ],
  },
  {
    name: 'ASUS ZenBook 14 OLED (Ultra 7/ 16GB/ 512GB/ 14" 2.8K)', brand: 'ASUS', cat: 'laptop',
    price: 22990000, origPrice: 25990000, sold: 167, rating: 4.6, reviews: 92,
    specChips: [
      { label: 'CPU', value: 'Ultra 7', icon: 'tool' },
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
      { label: 'CPU', value: 'i7-13700H', icon: 'tool' },
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
      { label: 'CPU', value: 'Ultra 7', icon: 'tool' },
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
      { label: 'CPU', value: 'i5-13500H', icon: 'tool' },
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
      { label: 'Nhân/Luồng', value: '20/28', icon: 'tool' },
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
      { label: 'CPU', value: 'i7-11700', icon: 'tool' },
      { label: 'RAM', value: '16GB', icon: 'database' },
      { label: 'Ổ cứng', value: '1TB SSD', icon: 'folder' },
      { label: 'OS', value: 'Win 11 Pro', icon: 'solution' },
    ],
  },
  {
    name: 'PC GVN Intel Ultra 9 285K/ RTX 5090 (Z890/ 64GB/ 1TB)', brand: 'INTEL', cat: 'pc',
    price: 206990000, origPrice: 213720000, sold: 12, rating: 5.0, reviews: 8,
    specChips: [
      { label: 'CPU', value: 'Ultra 9 285K', icon: 'tool' },
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
      { label: 'CPU', value: 'R9-9950X3D', icon: 'tool' },
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
