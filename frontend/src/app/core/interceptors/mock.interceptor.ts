import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';

interface LoginBody { email: string; password: string; }
interface RegisterBody { fullName: string; email: string; phone: string; password: string; }
interface CartAddBody { skuId: number; quantity: number; }
interface CartUpdateBody { skuId: number; quantity: number; }
interface VoucherBody { code: string; }
interface CartItem {
  skuId: number; name: string; slug: string; imageUrl: string;
  price: number; quantity: number; stock: number;
  brandName: string; selected: boolean;
}

@Injectable()
export class MockInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const { method, url, body } = req;

    // ── AUTH ──
    if (url.includes('/api/auth/login') && method === 'POST') {
      const { email, password } = body as LoginBody;
      if (email === 'admin@dawn.vn' && password === '123456') {
        return ok(this.mockUserSession('admin@dawn.vn', 'Admin', 'ADMIN'));
      }
      if (email && password && password.length >= 6) {
        return ok(this.mockUserSession(email, email.split('@')[0], 'CUSTOMER'));
      }
      return err(401, 'Email hoặc mật khẩu không đúng');
    }

    if (url.includes('/api/auth/register') && method === 'POST') {
      const d = body as RegisterBody;
      return ok({ id: Date.now(), fullName: d.fullName, email: d.email, phone: d.phone, systemRole: 'CUSTOMER' });
    }

    if (url.includes('/api/auth/refresh') && method === 'POST') {
      return ok(this.mockUserSession('user@dawn.vn', 'User', 'CUSTOMER'));
    }

    // ── CATALOG ──
    if (url.includes('/api/catalog/categories') && method === 'GET') {
      return of(new HttpResponse({ status: 200, body: this.categories }));
    }

    if (url.includes('/api/catalog/brands') && method === 'GET') {
      return of(new HttpResponse({ status: 200, body: this.brands }));
    }

    if (url.match(/\/api\/catalog\/skus\/([^/]+)\/reviews/) && method === 'GET') {
      return ok(this.reviews);
    }

    if (url.match(/\/api\/catalog\/skus\/([^/]+)\/related/) && method === 'GET') {
      const slug = url.split('/').pop()!;
      const all = this.generateProducts();
      const idx = all.findIndex(p => p.slug === slug);
      return ok(all.filter((_, i) => i !== idx).slice(0, 4));
    }

    if (url.match(/\/api\/catalog\/skus\/([^/]+)/) && method === 'GET') {
      const slug = url.split('/').pop()!;
      const p = this.generateProducts().find(pr => pr.slug === slug) || null;
      return p ? ok(p) : err(404, 'Not found');
    }

    if (url.includes('/api/catalog/skus') && method === 'GET') {
      let filtered = this.generateProducts();
      const params = this.parseParams(req);

      if (params['category']) filtered = filtered.filter(p => p.categoryName === params['category']);
      if (params['brand']) filtered = filtered.filter(p => p.brandName?.toLowerCase() === params['brand']!.toLowerCase());
      if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
      if (params['minPrice']) filtered = filtered.filter(p => p.price >= Number(params['minPrice']));
      if (params['maxPrice']) filtered = filtered.filter(p => p.price <= Number(params['maxPrice']));
      if (params['sort']) {
        switch (params['sort']) {
          case 'price-asc': filtered.sort((a, b) => a.price - b.price); break;
          case 'price-desc': filtered.sort((a, b) => b.price - a.price); break;
          case 'sold': filtered.sort((a, b) => b.soldCount - a.soldCount); break;
        }
      }

      const page = params['page'] ? Number(params['page']) : 0;
      const size = params['size'] ? Number(params['size']) : 20;
      const start = page * size;
      const paged = filtered.slice(start, start + size);
      return ok({
        content: paged,
        pagination: { pageNumber: page, pageSize: size, totalElements: filtered.length },
      });
    }

    // ── CART ──
    if (url.includes('/api/cart') && method === 'GET') {
      return ok({ items: this.cartItems, version: 1 });
    }

    if (url.includes('/api/cart/items') && method === 'POST') {
      const d = body as CartAddBody;
      const products = this.generateProducts();
      const sku = products.find(p => p.id === d.skuId);
      if (sku) {
        const exists = this.cartItems.findIndex(i => i.skuId === d.skuId);
        if (exists >= 0) {
          this.cartItems[exists] = { ...this.cartItems[exists], quantity: this.cartItems[exists].quantity + (d.quantity || 1) };
        } else {
          this.cartItems.unshift({
            skuId: sku.id, name: sku.name, slug: sku.slug, imageUrl: sku.imageUrls[0],
            price: sku.price, quantity: d.quantity || 1, stock: sku.stockAvailable || 99,
            brandName: sku.brandName, selected: true,
          });
        }
      }
      return ok({ items: this.cartItems, version: 2 });
    }

    if (url.match(/\/api\/cart\/items\/(\d+)/) && method === 'PUT') {
      const id = Number(url.match(/\/api\/cart\/items\/(\d+)/)![1]);
      const d = body as CartUpdateBody;
      const idx = this.cartItems.findIndex(i => i.skuId === id);
      if (idx >= 0) {
        this.cartItems[idx] = { ...this.cartItems[idx], quantity: d.quantity };
      }
      return ok({ items: this.cartItems, version: 2 });
    }

    if (url.match(/\/api\/cart\/items\/(\d+)/) && method === 'DELETE') {
      const id = Number(url.match(/\/api\/cart\/items\/(\d+)/)![1]);
      this.cartItems = this.cartItems.filter(i => i.skuId !== id);
      return ok({ items: this.cartItems, version: 2 });
    }

    if (url.includes('/api/cart') && method === 'DELETE') {
      this.cartItems = [];
      return ok({ items: [], version: null });
    }

    // ── ORDERS ──
    if (url.includes('/api/orders/my') && method === 'GET') {
      return ok({ content: this.mockOrders, pagination: { pageNumber: 0, pageSize: 20, totalElements: this.mockOrders.length } });
    }

    if (url.match(/\/api\/orders\/(\d+)\/cancel/) && method === 'PUT') {
      return ok(null);
    }

    if (url.match(/\/api\/orders\/(\d+)\/items\/(\d+)\/reviews/) && method === 'POST') {
      return ok({ id: Date.now(), ...(body as Record<string, unknown>), createdAt: new Date().toISOString() });
    }

    if (url.match(/\/api\/orders\/(\d+)/) && method === 'GET') {
      const id = Number(url.match(/\/api\/orders\/(\d+)/)![1]);
      const order = this.mockOrders.find(o => o.id === id);
      return order ? ok(order) : err(404, 'Not found');
    }

    if (url.includes('/api/orders/checkout') && method === 'POST') {
      return ok({ id: Date.now(), totalAmount: 25990000, paymentStatus: 'PENDING', paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html' });
    }

    // ── PAYMENT ──
    if (url.includes('/api/payments/vnpay/initiate') && method === 'POST') {
      return ok({ paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=' + Date.now() });
    }

    // ── PROMOTION ──
    if (url.includes('/api/promotion/flash-sales/active') && method === 'GET') {
      return ok(this.flashSaleCampaign);
    }

    if (url.includes('/api/promotions/vouchers/validate') && method === 'POST') {
      const d = body as VoucherBody;
      if (d.code === 'WELCOME10') {
        return ok({ valid: true, code: 'WELCOME10', discount: 500000, type: 'FIXED', minOrder: 1000000 });
      }
      return ok({ valid: false, message: 'Mã giảm giá không hợp lệ' });
    }

    if (url.includes('/api/promotions/vouchers') && method === 'GET') {
      return ok([
        { id: 1, code: 'WELCOME10', description: 'Giảm 500k cho đơn hàng đầu tiên', discount: 500000, type: 'FIXED', minOrder: 1000000, expiry: '31/12/2026' },
        { id: 2, code: 'SALE10', description: 'Giảm 10% cho đơn hàng trên 5 triệu', discount: 10, type: 'PERCENT', maxDiscount: 1000000, minOrder: 5000000, expiry: '31/12/2026' },
      ]);
    }

    // ── ADMIN ──
    if (url.includes('/api/admin/dashboard/summary') && method === 'GET') {
      return ok(this.dashboardSummary);
    }

    if (url.includes('/api/admin/dashboard/revenue') && method === 'GET') {
      return ok(Array.from({ length: 7 }, (_, i) => ({
        date: new Date(Date.now() - (6 - i) * 86400000).toISOString().split('T')[0],
        revenue: Math.floor(Math.random() * 50000000) + 10000000,
      })));
    }

    if (url.includes('/api/admin/orders') && method === 'GET') {
      return ok({ content: this.mockOrders, pagination: { pageNumber: 0, pageSize: 20, totalElements: this.mockOrders.length } });
    }

    if (url.match(/\/api\/admin\/orders\/(\d+)\/status/) && method === 'PUT') {
      return ok(null);
    }

    // ── INVENTORY ──
    if (url.includes('/api/inventory') && method === 'GET') {
      const products = this.generateProducts();
      return ok(products.map(p => ({
        skuId: p.id, name: p.name, slug: p.slug, stock: p.stockAvailable || Math.floor(Math.random() * 100) + 5,
        price: p.price, imageUrl: p.imageUrls[0], brandName: p.brandName,
      })));
    }

    if (url.match(/\/api\/inventory\/(\d+)\/restock/) && method === 'POST') {
      return ok({ success: true });
    }

    // ── BUILDER ──
    if (url.includes('/api/builder/slots') && method === 'GET') {
      return ok(['CPU', 'Mainboard', 'RAM', 'VGA', 'SSD', 'HDD', 'PSU', 'Case', 'CPU Cooler', 'Monitor']);
    }

    if (url.includes('/api/builder/validate') && method === 'POST') {
      return ok({ valid: true, warnings: [], errors: [] });
    }

    if (url.includes('/api/builder/templates') && method === 'GET') {
      return ok([
        { id: 1, name: 'PC Gaming Tầm Trung', totalPrice: 25990000, slots: { CPU: 'Intel i5-13400F', VGA: 'RTX 4060', RAM: '16GB DDR5' } },
        { id: 2, name: 'PC Gaming Cao Cấp', totalPrice: 45990000, slots: { CPU: 'Intel i7-14700K', VGA: 'RTX 4070 Ti', RAM: '32GB DDR5' } },
        { id: 3, name: 'PC Workstation', totalPrice: 65990000, slots: { CPU: 'AMD R9-7950X', VGA: 'RTX 4080', RAM: '64GB DDR5' } },
      ]);
    }

    if (url.match(/\/api\/builder\/templates\/(\d+)/) && method === 'GET') {
      return ok({ id: 1, name: 'PC Gaming Tầm Trung', totalPrice: 25990000, slots: { CPU: 'Intel i5-13400F', VGA: 'RTX 4060', RAM: '16GB DDR5', Mainboard: 'B760M', SSD: '512GB NVMe', PSU: '750W' } });
    }

    // ── PASS THROUGH (non-API or unmatched) ──
    return next.handle(req);

    // ── HELPERS ──
    function ok(body: unknown, status = 200) {
      return of(new HttpResponse({ status, body })).pipe(delay(80 + Math.random() * 120));
    }

    function err(status: number, message: string) {
      return of(new HttpResponse({ status, statusText: message, body: { message } }));
    }
  }

  private parseParams(req: HttpRequest<unknown>): Record<string, string> {
    const params: Record<string, string> = {};
    if (req.params) {
      req.params.keys().forEach(k => { params[k] = req.params.get(k)!; });
    }
    return params;
  }

  private mockUserSession(email: string, name: string, role: string) {
    return {
      accessToken: 'mock-token-' + Date.now(),
      refreshToken: 'mock-refresh-' + Date.now(),
      user: { id: 1, fullName: name, email, phone: '0912345678', systemRole: role },
    };
  }

  // ── DATA ──
  private readonly baseUrl = 'https://placehold.co';

  private categories = [
    { id: 1, name: 'Laptop', slug: 'laptop', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=L` },
    { id: 2, name: 'Laptop Gaming', slug: 'laptop-gaming', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=LG` },
    { id: 3, name: 'PC GVN', slug: 'pc', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PC` },
    { id: 4, name: 'Main, CPU, VGA', slug: 'linh-kien', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=CPU` },
    { id: 5, name: 'Case, Nguồn, Tản', slug: 'case-nguon-tan', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PSU` },
    { id: 6, name: 'Ổ cứng, RAM, Thẻ nhớ', slug: 'o-cung-ram', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=SSD` },
    { id: 7, name: 'Loa, Micro, Webcam', slug: 'loa-micro-webcam', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=LOA` },
    { id: 8, name: 'Màn hình', slug: 'man-hinh', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=MH` },
    { id: 9, name: 'Bàn phím', slug: 'ban-phim', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=BP` },
    { id: 10, name: 'Chuột + Lót chuột', slug: 'chuot', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=CH` },
    { id: 11, name: 'Tai Nghe', slug: 'tai-nghe', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=TN` },
    { id: 12, name: 'Ghế - Bàn', slug: 'ghe-ban', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=GB` },
    { id: 13, name: 'Phần mềm, mạng', slug: 'phan-mem-mang', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PM` },
    { id: 14, name: 'Handheld, Console', slug: 'handheld-console', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=HC` },
    { id: 15, name: 'Phụ kiện (Hub, sạc, cáp)', slug: 'phu-kien', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=PK` },
    { id: 16, name: 'Dịch vụ', slug: 'dich-vu', parentId: null, imageUrl: `${this.baseUrl}/80x80/1e293b/white?text=DV` },
  ];

  private brands = [
    { id: 1, name: 'ASUS', slug: 'asus' }, { id: 2, name: 'MSI', slug: 'msi' },
    { id: 3, name: 'ACER', slug: 'acer' }, { id: 4, name: 'LENOVO', slug: 'lenovo' },
    { id: 5, name: 'DELL', slug: 'dell' }, { id: 6, name: 'SAMSUNG', slug: 'samsung' },
    { id: 7, name: 'LG', slug: 'lg' }, { id: 8, name: 'LOGITECH', slug: 'logitech' },
    { id: 9, name: 'GIGABYTE', slug: 'gigabyte' }, { id: 10, name: 'INTEL', slug: 'intel' },
    { id: 11, name: 'AMD', slug: 'amd' }, { id: 12, name: 'NVIDIA', slug: 'nvidia' },
  ];

  private cartItems: CartItem[] = [];

  private reviews = [
    { id: 1, userId: 1, fullName: 'Nguyễn Văn A', rating: 5, comment: 'Sản phẩm rất tốt, đóng gói cẩn thận, giao hàng nhanh.', createdAt: new Date(Date.now() - 86400000 * 2).toISOString() },
    { id: 2, userId: 2, fullName: 'Trần Thị B', rating: 4, comment: 'Chất lượng tuyệt vời, giá hợp lý. Sẽ mua thêm.', createdAt: new Date(Date.now() - 86400000 * 5).toISOString() },
    { id: 3, userId: 3, fullName: 'Lê Văn C', rating: 5, comment: 'Hàng chính hãng, máy chạy mượt, nhiệt độ thấp.', createdAt: new Date(Date.now() - 86400000 * 7).toISOString() },
  ];

  private mockOrders = [
    {
      id: 1001, createdAt: new Date(Date.now() - 86400000 * 3).toISOString(),
      totalAmount: 28790000, paymentStatus: 'PAID', shippingAddress: { fullName: 'Nguyễn Văn A', phone: '0912345678', street: '123 Nguyễn Huệ', district: 'Quận 1', city: 'TP HCM' },
      subOrders: [{ id: 2001, status: 'DELIVERED', fulfillmentType: 'DELIVERY', subtotal: 28790000, shippingFee: 0, discountAmount: 0, finalAmount: 28790000,
        items: [{ id: 3001, skuId: 1, name: 'Laptop gaming Gigabyte A16', imageUrl: `${this.baseUrl}/400x400/1e293b/white?text=Gigabyte`, quantity: 1, unitPrice: 28790000 }] }],
    },
    {
      id: 1002, createdAt: new Date(Date.now() - 86400000 * 10).toISOString(),
      totalAmount: 15990000, paymentStatus: 'PAID', shippingAddress: { fullName: 'Trần Thị B', phone: '0987654321', street: '456 Lê Lợi', district: 'Hải Châu', city: 'Đà Nẵng' },
      subOrders: [{ id: 2002, status: 'PROCESSING', fulfillmentType: 'DELIVERY', subtotal: 15990000, shippingFee: 0, discountAmount: 0, finalAmount: 15990000,
        items: [{ id: 3002, skuId: 13, name: 'NVIDIA RTX 4070 Super 12GB', imageUrl: `${this.baseUrl}/400x400/1e293b/white?text=RTX+4070`, quantity: 1, unitPrice: 15990000 }] }],
    },
    {
      id: 1003, createdAt: new Date(Date.now() - 86400000 * 15).toISOString(),
      totalAmount: 17990000, paymentStatus: 'PENDING', shippingAddress: { fullName: 'Lê Văn C', phone: '0909123456', street: '789 Trần Hưng Đạo', district: 'Thanh Khê', city: 'Đà Nẵng' },
      subOrders: [{ id: 2003, status: 'PENDING_PAYMENT', fulfillmentType: 'PICKUP', subtotal: 17990000, shippingFee: 0, discountAmount: 0, finalAmount: 17990000,
        items: [{ id: 3003, skuId: 7, name: 'PC GVN I3-12100F/ RTX 5050', imageUrl: `${this.baseUrl}/400x400/1e293b/white?text=PC+I3`, quantity: 1, unitPrice: 17990000 }] }],
    },
  ];

  private flashSaleCampaign = {
    id: 1, campaignName: 'FLASH SALE - DEAL GẤP RÚT',
    endTime: new Date(Date.now() + 3 * 3600 * 1000).toISOString(),
    items: [
      { skuId: 13, name: 'NVIDIA RTX 4070 Super 12GB', slug: 'rtx-4070-super', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=RTX+4070`, originalPrice: 18990000, salePrice: 15990000, remaining: 8, total: 20, category: 'Linh kiện', discountPercent: 16 },
      { skuId: 1, name: 'Gigabyte A16 i5-13420H', slug: 'gigabyte-a16', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=Gigabyte`, originalPrice: 30790000, salePrice: 28790000, remaining: 5, total: 15, category: 'Laptop', discountPercent: 6 },
      { skuId: 20, name: 'Logitech G Pro X Superlight 2', slug: 'g-pro-x-superlight-2', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=Superlight`, originalPrice: 3990000, salePrice: 3290000, remaining: 12, total: 30, category: 'Phụ kiện', discountPercent: 18 },
      { skuId: 17, name: 'Samsung Odyssey OLED G8 34"', slug: 'odyssey-oled-g8', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=Odyssey`, originalPrice: 29990000, salePrice: 25990000, remaining: 3, total: 10, category: 'Màn hình', discountPercent: 13 },
      { skuId: 15, name: 'Intel Core i7-14700K', slug: 'i7-14700k', imageUrl: `${this.baseUrl}/200x200/e53935/white?text=i7-14700K`, originalPrice: 10990000, salePrice: 8990000, remaining: 15, total: 40, category: 'Linh kiện', discountPercent: 18 },
    ],
  };

  private dashboardSummary = {
    totalRevenue: 256900000,
    totalOrders: 1234,
    totalProducts: 22,
    totalCustomers: 892,
    recentOrders: [
      { id: 1001, customerName: 'Nguyễn Văn A', total: 28790000, status: 'COMPLETED', createdAt: new Date(Date.now() - 86400000 * 3).toISOString() },
      { id: 1002, customerName: 'Trần Thị B', total: 15990000, status: 'PROCESSING', createdAt: new Date(Date.now() - 86400000 * 10).toISOString() },
      { id: 1003, customerName: 'Lê Văn C', total: 17990000, status: 'PENDING', createdAt: new Date(Date.now() - 86400000 * 15).toISOString() },
    ],
  };

  private productTemplates: {
    name: string; brand: string; cat: string; price: number; origPrice: number;
    sold: number; rating: number; reviews: number; specChips: { label: string; value: string; icon: string }[];
  }[] = [
    { name: 'Laptop gaming Gigabyte A16 CMHH2VN893SH (Core i5-13420H/ RTX 4050/ 16GB/ 512GB/ Win 11)', brand: 'GIGABYTE', cat: 'laptop-gaming', price: 28790000, origPrice: 30790000, sold: 342, rating: 4.5, reviews: 128, specChips: [{ label: 'CPU', value: 'i5-13420H', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 4050', icon: 'thunderbolt' }, { label: 'Màn hình', value: '16" FHD+', icon: 'monitor' }, { label: 'Hz', value: '165Hz', icon: 'eye' }] },
    { name: 'Laptop gaming Gigabyte A16 GA63H 3THK3VN893SH (R7-260/ RTX 5050/ 16GB/ 512GB)', brand: 'GIGABYTE', cat: 'laptop-gaming', price: 38990000, origPrice: 40990000, sold: 156, rating: 4.3, reviews: 64, specChips: [{ label: 'CPU', value: 'R7-260', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 5050', icon: 'thunderbolt' }, { label: 'Màn hình', value: '16" FullHD+', icon: 'monitor' }, { label: 'Hz', value: '165Hz', icon: 'eye' }] },
    { name: 'Laptop gaming ACER Nitro ProPanel ANV15-41-R9M1 (R5-7535HS/ RTX 3050/ 16GB/ 512GB/ 15.6" FHD)', brand: 'ACER', cat: 'laptop-gaming', price: 23990000, origPrice: 27990000, sold: 89, rating: 4.0, reviews: 32, specChips: [{ label: 'CPU', value: 'R5-7535HS', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 3050', icon: 'thunderbolt' }, { label: 'Màn hình', value: '15.6" FHD', icon: 'monitor' }, { label: 'Hz', value: '180Hz', icon: 'eye' }] },
    { name: 'Laptop gaming LENOVO Legion Pro 5 16IRX9 (i9-14900HX/ RTX 4070/ 32GB/ 1TB/ 16" 2.5K)', brand: 'LENOVO', cat: 'laptop-gaming', price: 37990000, origPrice: 42990000, sold: 201, rating: 4.7, reviews: 203, specChips: [{ label: 'CPU', value: 'i9-14900HX', icon: 'cpu' }, { label: 'RAM', value: '32GB', icon: 'database' }, { label: 'Ổ cứng', value: '1TB', icon: 'folder' }, { label: 'GPU', value: 'RTX 4070', icon: 'thunderbolt' }, { label: 'Màn hình', value: '16" 2.5K', icon: 'monitor' }, { label: 'Hz', value: '240Hz', icon: 'eye' }] },
    { name: 'PC GVN Intel i5-12400F/ VGA RTX 5060 (Main H610/ 16GB/ 512GB)', brand: 'INTEL', cat: 'pc', price: 23990000, origPrice: 24520000, sold: 123, rating: 4.6, reviews: 67, specChips: [{ label: 'CPU', value: 'i5-12400F', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 5060', icon: 'thunderbolt' }, { label: 'Main', value: 'H610', icon: 'build' }] },
    { name: 'PC GVN Intel i7-14700F/ VGA RTX 5060 Ti (B760M/ 16GB/ 512GB)', brand: 'INTEL', cat: 'pc', price: 35990000, origPrice: 36320000, sold: 78, rating: 4.8, reviews: 45, specChips: [{ label: 'CPU', value: 'i7-14700F', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 5060 Ti', icon: 'thunderbolt' }, { label: 'Main', value: 'B760M', icon: 'build' }] },
    { name: 'PC GVN I3-12100F/ VGA RTX 5050 (H610/ 8GB/ 256GB)', brand: 'INTEL', cat: 'pc', price: 17990000, origPrice: 19620000, sold: 234, rating: 4.3, reviews: 156, specChips: [{ label: 'CPU', value: 'i3-12100F', icon: 'cpu' }, { label: 'RAM', value: '8GB', icon: 'database' }, { label: 'Ổ cứng', value: '256GB', icon: 'folder' }, { label: 'GPU', value: 'RTX 5050', icon: 'thunderbolt' }, { label: 'Main', value: 'H610', icon: 'build' }] },
    { name: 'PC GVN AMD R7-7800X3D/ RTX 5060 (DDR5)', brand: 'AMD', cat: 'pc', price: 42990000, origPrice: 44220000, sold: 45, rating: 4.9, reviews: 28, specChips: [{ label: 'CPU', value: 'R7-7800X3D', icon: 'cpu' }, { label: 'RAM', value: '32GB DDR5', icon: 'database' }, { label: 'Ổ cứng', value: '1TB', icon: 'folder' }, { label: 'GPU', value: 'RTX 5060', icon: 'thunderbolt' }] },
    { name: 'ASUS ZenBook 14 OLED (Ultra 7/ 16GB/ 512GB/ 14" 2.8K)', brand: 'ASUS', cat: 'laptop', price: 22990000, origPrice: 25990000, sold: 167, rating: 4.6, reviews: 92, specChips: [{ label: 'CPU', value: 'Ultra 7', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'Màn hình', value: '14" 2.8K OLED', icon: 'monitor' }, { label: 'Trọng lượng', value: '1.2 kg', icon: 'column-height' }] },
    { name: 'DELL XPS 15 9530 (i7-13700H/ 16GB/ 512GB/ 15.6" OLED)', brand: 'DELL', cat: 'laptop', price: 32990000, origPrice: 35990000, sold: 89, rating: 4.4, reviews: 45, specChips: [{ label: 'CPU', value: 'i7-13700H', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'Màn hình', value: '15.6" OLED', icon: 'monitor' }, { label: 'GPU', value: 'RTX 4060', icon: 'thunderbolt' }] },
    { name: 'LG Gram 16 2024 (Ultra 7/ 16GB/ 512GB/ 16" WQXGA)', brand: 'LG', cat: 'laptop', price: 28990000, origPrice: 31990000, sold: 78, rating: 4.5, reviews: 38, specChips: [{ label: 'CPU', value: 'Ultra 7', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'Màn hình', value: '16" WQXGA', icon: 'monitor' }, { label: 'Trọng lượng', value: '1.19 kg', icon: 'column-height' }] },
    { name: 'MSI Modern 15 H (i5-13500H/ 16GB/ 512GB/ 15.6" FHD)', brand: 'MSI', cat: 'laptop', price: 15990000, origPrice: 18990000, sold: 234, rating: 4.2, reviews: 156, specChips: [{ label: 'CPU', value: 'i5-13500H', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '512GB', icon: 'folder' }, { label: 'Màn hình', value: '15.6" FHD', icon: 'monitor' }] },
    { name: 'NVIDIA RTX 4070 Super Gaming X Trio 12GB', brand: 'MSI', cat: 'linh-kien', price: 15990000, origPrice: 18990000, sold: 234, rating: 4.7, reviews: 189, specChips: [{ label: 'Dung lượng', value: '12GB GDDR6X', icon: 'thunderbolt' }, { label: 'CUDA', value: '7168 Cores', icon: 'build' }, { label: 'Kết nối', value: 'HDMI 2.1a x3', icon: 'scan' }, { label: 'Nguồn', value: '700W', icon: 'thunderbolt' }] },
    { name: 'Intel Core i7-14700K (20 nhân/ 28 luồng/ 5.6GHz)', brand: 'INTEL', cat: 'linh-kien', price: 8990000, origPrice: 10990000, sold: 345, rating: 4.5, reviews: 210, specChips: [{ label: 'Nhân/Luồng', value: '20/28', icon: 'cpu' }, { label: 'Xung nhịp', value: '5.6 GHz', icon: 'thunderbolt' }, { label: 'TDP', value: '125W', icon: 'build' }, { label: 'Socket', value: 'LGA1700', icon: 'tool' }] },
    { name: 'Samsung Odyssey OLED G8 34" (3440x1440/ 175Hz/ 0.03ms)', brand: 'SAMSUNG', cat: 'man-hinh', price: 25990000, origPrice: 29990000, sold: 67, rating: 4.6, reviews: 42, specChips: [{ label: 'Kích thước', value: '34"', icon: 'monitor' }, { label: 'Độ phân giải', value: '3440x1440', icon: 'eye' }, { label: 'Tần số', value: '175Hz', icon: 'thunderbolt' }, { label: 'Panel', value: 'OLED', icon: 'build' }] },
    { name: 'LG UltraGear 27GP850 (27" QHD/ 165Hz/ IPS)', brand: 'LG', cat: 'man-hinh', price: 8990000, origPrice: 10990000, sold: 178, rating: 4.4, reviews: 95, specChips: [{ label: 'Kích thước', value: '27"', icon: 'monitor' }, { label: 'Độ phân giải', value: '2560x1440', icon: 'eye' }, { label: 'Tần số', value: '165Hz', icon: 'thunderbolt' }, { label: 'Panel', value: 'IPS', icon: 'build' }] },
    { name: 'Logitech G Pro X Superlight 2 (Wireless/ 32000DPI/ 60g)', brand: 'LOGITECH', cat: 'phu-kien', price: 3290000, origPrice: 3990000, sold: 456, rating: 4.7, reviews: 312, specChips: [{ label: 'Kết nối', value: 'Wireless', icon: 'scan' }, { label: 'DPI', value: '32000', icon: 'eye' }, { label: 'Trọng lượng', value: '60g', icon: 'column-height' }, { label: 'Pin', value: '95h', icon: 'thunderbolt' }] },
    { name: 'Logitech G915 X Lightspeed (Wireless/ RGB/ Low Profile)', brand: 'LOGITECH', cat: 'phu-kien', price: 4990000, origPrice: 5990000, sold: 234, rating: 4.5, reviews: 156, specChips: [{ label: 'Kết nối', value: 'Lightspeed', icon: 'scan' }, { label: 'Switch', value: 'Low Profile', icon: 'tool' }, { label: 'Pin', value: '40h', icon: 'thunderbolt' }] },
    { name: 'Máy bộ ACER Altos P130F7 (i7/ RAM 16GB/ SSD 1TB)', brand: 'ACER', cat: 'pc', price: 16990000, origPrice: 17490000, sold: 56, rating: 4.2, reviews: 23, specChips: [{ label: 'CPU', value: 'i7-11700', icon: 'cpu' }, { label: 'RAM', value: '16GB', icon: 'database' }, { label: 'Ổ cứng', value: '1TB SSD', icon: 'folder' }, { label: 'OS', value: 'Win 11 Pro', icon: 'solution' }] },
    { name: 'PC GVN Intel Ultra 9 285K/ RTX 5090 (Z890/ 64GB/ 1TB)', brand: 'INTEL', cat: 'pc', price: 206990000, origPrice: 213720000, sold: 12, rating: 5.0, reviews: 8, specChips: [{ label: 'CPU', value: 'Ultra 9 285K', icon: 'cpu' }, { label: 'RAM', value: '64GB DDR5', icon: 'database' }, { label: 'Ổ cứng', value: '1TB NVMe', icon: 'folder' }, { label: 'GPU', value: 'RTX 5090', icon: 'thunderbolt' }, { label: 'Main', value: 'Z890', icon: 'build' }] },
    { name: 'PC GVN x ASUS Extreme R9-9950X3D/ RTX 5090 (X870E/ 64GB/ 1TB)', brand: 'AMD', cat: 'pc', price: 265990000, origPrice: 267920000, sold: 8, rating: 5.0, reviews: 5, specChips: [{ label: 'CPU', value: 'R9-9950X3D', icon: 'cpu' }, { label: 'RAM', value: '64GB DDR5', icon: 'database' }, { label: 'Ổ cứng', value: '1TB NVMe', icon: 'folder' }, { label: 'GPU', value: 'RTX 5090', icon: 'thunderbolt' }, { label: 'Main', value: 'X870E', icon: 'build' }] },
    { name: 'ASUS ROG Harpe Ace Aim Lab (Wireless/ 36000DPI/ 54g)', brand: 'ASUS', cat: 'phu-kien', price: 2790000, origPrice: 0, sold: 167, rating: 4.4, reviews: 89, specChips: [{ label: 'Kết nối', value: 'Wireless', icon: 'scan' }, { label: 'DPI', value: '36000', icon: 'eye' }, { label: 'Trọng lượng', value: '54g', icon: 'column-height' }] },
  ];

  private generateProducts() {
    return this.productTemplates.map((t, i) => ({
      id: i + 1,
      name: t.name,
      slug: t.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '').substring(0, 120),
      price: t.price,
      originalPrice: t.origPrice || undefined,
      imageUrls: [`${this.baseUrl}/400x400/1e293b/white?text=${encodeURIComponent(t.name.substring(0, 20))}`, `${this.baseUrl}/400x400/e53935/white?text=${encodeURIComponent(t.name.substring(0, 20))}`],
      brandName: t.brand,
      status: 'ACTIVE',
      soldCount: t.sold,
      rating: t.rating,
      reviewCount: t.reviews,
      specChips: t.specChips,
      specs: t.specChips.reduce((acc: Record<string, string>, chip) => ({ ...acc, [chip.label]: chip.value }), {}),
      description: `${t.name} — Sản phẩm cao cấp chính hãng. Được kiểm tra kỹ lưỡng trước khi bán. Bảo hành 24 tháng.`,
      stockAvailable: Math.floor(Math.random() * 50) + 5,
      categoryName: t.cat,
    }));
  }
}
