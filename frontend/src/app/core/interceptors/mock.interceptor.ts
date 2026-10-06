import { HttpRequest, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, delay } from 'rxjs';
import { PRODUCT_TEMPLATES } from '@core/mocks/product-template.mock';
import { MOCK_CATEGORIES, MOCK_BRANDS } from '@core/mocks/category.mock';
import { MOCK_ORDERS } from '@core/mocks/order.mock';
import { MOCK_DASHBOARD_SUMMARY, MOCK_REVENUE_7_DAYS, MOCK_LOW_STOCK_ITEMS } from '@core/mocks/dashboard.mock';
import { MOCK_VOUCHERS } from '@core/mocks/voucher.mock';
import { MOCK_BUILDER_SLOTS, MOCK_BUILDER_TEMPLATES } from '@core/mocks/builder.mock';
import { MOCK_ADMIN_USERS } from '@core/mocks/admin-user.mock';
import { MOCK_RETURN_REQUESTS } from '@core/mocks/return-request.mock';
import { MOCK_AUDIT_LOGS } from '@core/mocks/audit-log.mock';
import { MOCK_ADMIN_REVIEWS } from '@core/mocks/admin-review.mock';
import { Address, Category, Sku, User } from '@core/models/common.model';
import { inject } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

interface CartAddBody { skuId: number; quantity: number; }
interface CartUpdateBody { skuId: number; quantity: number; }
interface VoucherBody { code: string; }
interface CartItem {
  skuId: number; name: string; slug: string; imageUrl: string;
  price: number; quantity: number; stock: number;
  brandName: string; selected: boolean;
}

const baseUrl = 'https://dummyimage.com';

// ponytail: mutable local copy so imports aren't mutated across requests
let cartItems: CartItem[] = [];
const localOrders = [...MOCK_ORDERS];


let userAddresses: Address[] = [];
let adminUsers: User[] = [...MOCK_ADMIN_USERS];
let adminCategories: Category[] = [...MOCK_CATEGORIES];
const returnRequests = [...MOCK_RETURN_REQUESTS];
const reviewQueue = [...MOCK_ADMIN_REVIEWS];
let nextAdminId = 1000;

function generateAdminProducts(): Sku[] {
  return PRODUCT_TEMPLATES.map((t, i) => ({
    id: i + 1,
    name: t.name,
    slug: t.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '').substring(0, 120),
    price: t.price,
    originalPrice: t.origPrice || undefined,
    imageUrls: [`${baseUrl}/400x400/1e293b/white?text=${encodeURIComponent(t.name.substring(0, 20))}`],
    brandName: t.brand,
    status: 'ACTIVE',
    soldCount: t.sold,
    rating: t.rating,
    reviewCount: t.reviews,
    specChips: t.specChips,
    categoryName: t.cat,
  })) as Sku[];
}

let adminProducts: Sku[] = generateAdminProducts();

function ok(body: unknown, status = 200) {
  return of(new HttpResponse({ status, body })).pipe(delay(80 + Math.random() * 120));
}

function err(status: number, message: string) {
  return of(new HttpResponse({ status, statusText: message, body: { message } }));
}

function parseParams(req: HttpRequest<unknown>): Record<string, string> {
  const params: Record<string, string> = {};
  if (req.params) {
    req.params.keys().forEach(k => { params[k] = req.params.get(k)!; });
  }
  return params;
}

function mockUserSession(email: string, name: string, role: string) {
  return {
    accessToken: 'mock-token-' + Date.now(),
    refreshToken: 'mock-refresh-' + Date.now(),
    user: { id: 1, fullName: name, email, phone: '0912345678', systemRole: role },
  };
}

function generateProducts() {
  return PRODUCT_TEMPLATES.map((t, i) => ({
    id: i + 1,
    name: t.name,
    slug: t.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '').substring(0, 120),
    price: t.price,
    originalPrice: t.origPrice || undefined,
    imageUrls: [`${baseUrl}/400x400/1e293b/white?text=${encodeURIComponent(t.name.substring(0, 20))}`, `${baseUrl}/400x400/e53935/white?text=${encodeURIComponent(t.name.substring(0, 20))}`],
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

export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const { method, url, body } = req;

  // Token JWT thật (eyJ...) thì đi thẳng BE, không mock — tránh thế giới lai
  // nửa mock nửa thật: token mock + API thật = 401 -> refresh loop -> logout oan
  // ngay sau login. Mock chỉ phục vụ token mock-* và khách vãng lai.
  const authz = req.headers.get('Authorization') ?? '';
  if (/^Bearer eyJ/.test(authz)) {
    return next(req);
  }

  // ── AUTH ──
  if (url.includes('/api/v1/auth/refresh') && method === 'POST') {
    return ok(mockUserSession('user@dawn.vn', 'User', 'CUSTOMER'));
  }
  if (url.includes('/api/v1/auth/login') && method === 'POST') {
    const { email, password } = body as any;
    if (email === 'admin@dawn.vn' && password === 'admin123') {
      return ok(mockUserSession(email, 'Admin', 'ADMIN'));
    }
    return ok(mockUserSession(email, email.split('@')[0], 'CUSTOMER'));
  }
  if (url.includes('/api/v1/auth/register') && method === 'POST') {
    return ok(null);
  }
  if (url.includes('/api/v1/auth/verify-otp') && method === 'POST') {
    return ok(mockUserSession('user@dawn.vn', 'User', 'CUSTOMER'));
  }
  if (url.includes('/api/v1/auth/resend-otp') && method === 'POST') {
    return ok(null);
  }
  if (url.includes('/api/v1/auth/forgot-password') && method === 'POST') {
    return ok(null);
  }
  if (url.includes('/api/v1/auth/reset-password') && method === 'POST') {
    return ok(null);
  }

  // ── USER ──
  if (url.match(/\/api\/v1\/users\/me\/addresses\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/users\/me\/addresses\/(\d+)/)![1]);
    const idx = userAddresses.findIndex(a => a.id === id);
    if (idx >= 0) userAddresses[idx] = { ...userAddresses[idx], ...(body as Partial<Address>), id };
    return idx >= 0 ? ok(userAddresses[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/users\/me\/addresses\/(\d+)/) && method === 'DELETE') {
    const id = Number(url.match(/\/api\/v1\/users\/me\/addresses\/(\d+)/)![1]);
    userAddresses = userAddresses.filter(a => a.id !== id);
    return ok(null);
  }
  if (url.includes('/api/v1/users/me/addresses') && method === 'GET') {
    return ok(userAddresses);
  }
  if (url.includes('/api/v1/users/me/addresses') && method === 'POST') {
    const created: Address = { id: ++nextAdminId, ...(body as Omit<Address, 'id'>) };
    userAddresses = [...userAddresses, created];
    return ok(created);
  }
  if (url.includes('/api/v1/users/me') && method === 'GET') {
    const current = authService.currentUser();
    return ok(current ?? { id: 1, fullName: 'User', email: 'user@dawn.vn', phone: '0912345678', systemRole: 'CUSTOMER' });
  }
  if (url.includes('/api/v1/users/me') && method === 'PUT') {
    const current = authService.currentUser();
    return ok({ ...(current ?? { id: 1, email: 'user@dawn.vn', systemRole: 'CUSTOMER' }), ...(body as Record<string, unknown>) });
  }

  // ── CATALOG ──
  if (url.includes('/api/v1/brands') && method === 'GET') {
    return ok(MOCK_BRANDS);
  }

  if (url.includes('/api/v1/categories') && method === 'GET') {
    return ok(MOCK_CATEGORIES);
  }

  if (url.match(/\/api\/v1\/skus\/(\d+)/) && method === 'GET') {
    const id = Number(url.match(/\/api\/v1\/skus\/(\d+)/)![1]);
    const p = generateProducts().find(pr => pr.id === id) || null;
    return p ? ok(p) : err(404, 'Not found');
  }

  if (url.includes('/api/v1/skus/compare') && method === 'GET') {
    return ok(generateProducts().slice(0, 3));
  }

  if (url.includes('/api/v1/skus') && method === 'GET') {
    let filtered = generateProducts();
    const params = parseParams(req);

    if (params['categoryId']) filtered = filtered.filter(p => p.categoryName === params['categoryId']);
    if (params['brandId']) filtered = filtered.filter(p => p.brandName?.toLowerCase() === /* mock */ params['brandId']!.toLowerCase());
    if (params['search']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['search']!.toLowerCase()));
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
      page: page,
      size: size,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size),
    });
  }

  // ── CART ──
  if (url.includes('/api/v1/cart/count') && method === 'GET') {
    return ok(cartItems.reduce((s, i) => s + i.quantity, 0));
  }

  // Mock chỉ ôm giỏ member (demo offline). Giỏ guest đi API thật nên LOẠI /guest ra,
  // không thì ghi thật (POST lọt xuống BE) mà đọc mock (rỗng) -> thêm vào giỏ mà badge/cart không bao giờ hiện
  if (url.includes('/api/v1/cart') && !url.includes('/guest') && method === 'GET') {
    return ok({ items: cartItems, version: 1 });
  }

  if (url.includes('/api/v1/cart/items') && method === 'POST') {
    const d = body as CartAddBody;
    const products = generateProducts();
    const sku = products.find(p => p.id === d.skuId);
    if (sku) {
      const exists = cartItems.findIndex(i => i.skuId === d.skuId);
      if (exists >= 0) {
        cartItems[exists] = { ...cartItems[exists], quantity: cartItems[exists].quantity + (d.quantity || 1) };
      } else {
        cartItems.unshift({
          skuId: sku.id, name: sku.name, slug: sku.slug, imageUrl: sku.imageUrls[0],
          price: sku.price, quantity: d.quantity || 1, stock: sku.stockAvailable || 99,
          brandName: sku.brandName, selected: true,
        });
      }
    }
    return ok({ items: cartItems, version: 2 });
  }

  if (url.match(/\/api\/v1\/cart\/items\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/cart\/items\/(\d+)/)![1]);
    const d = body as CartUpdateBody;
    const idx = cartItems.findIndex(i => i.skuId === id);
    if (idx >= 0) {
      cartItems[idx] = { ...cartItems[idx], quantity: d.quantity };
    }
    return ok({ items: cartItems, version: 2 });
  }

  if (url.match(/\/api\/v1\/cart\/items\/(\d+)/) && method === 'DELETE') {
    const id = Number(url.match(/\/api\/v1\/cart\/items\/(\d+)/)![1]);
    cartItems = cartItems.filter(i => i.skuId !== id);
    return ok({ items: cartItems, version: 2 });
  }

  // ── ORDERS ──
  if (url.includes('/api/v1/orders/checkout') && method === 'POST') {
    const newId = Date.now();
    const newOrder = {
      id: newId,
      createdAt: new Date().toISOString(),
      totalAmount: 25990000,
      paymentStatus: 'PENDING',
      shippingAddress: (body as Record<string, any>)['shippingAddress'] ?? { fullName: 'Khách hàng', phone: '0900000000', street: '123 ABC', district: 'Quận 1', city: 'TP HCM' },
      subOrders: [{
        id: newId + 1,
        status: 'PENDING_PAYMENT',
        fulfillmentType: (body as Record<string, any>)['fulfillmentType'] ?? 'DELIVERY',
        subtotal: 25990000,
        shippingFee: 0,
        discountAmount: 0,
        finalAmount: 25990000,
        items: [{ id: newId + 2, skuId: 1, name: 'Đơn hàng mới', imageUrl: `${baseUrl}/400x400/1e293b/white?text=Order`, quantity: 1, unitPrice: 25990000 }],
      }],
    };
    localOrders.unshift(newOrder);
    return ok({ id: newId, totalAmount: 25990000, paymentStatus: 'PENDING' });
  }

  if (url.match(/\/api\/v1\/orders\/sub-orders\/(\d+)\/cancel/) && method === 'POST') {
    return ok(null);
  }

  if (url.includes('/api/v1/orders') && method === 'GET') {
    return ok({ content: localOrders, page: 0, size: 20, totalElements: localOrders.length, totalPages: 1 });
  }

  if (url.match(/\/api\/v1\/orders\/(\d+)/) && method === 'GET') {
    const id = Number(url.match(/\/api\/v1\/orders\/(\d+)/)![1]);
    const order = localOrders.find(o => o.id === id);
    return order ? ok(order) : err(404, 'Not found');
  }

  // ── STAFF ORDERS ──
  if (url.includes('/api/v1/staff/orders/sub-orders') && method === 'POST') {
    return ok(null);
  }
  if (url.includes('/api/v1/staff/orders') && method === 'GET') {
    return ok({ content: localOrders, page: 0, size: 20, totalElements: localOrders.length, totalPages: 1 });
  }

  // ── CARRIER ──
  if (url.match(/\/api\/v1\/sub-orders\/(\d+)\/assign-carrier/) && method === 'POST') {
    return ok(null);
  }
  if (url.match(/\/api\/v1\/sub-orders\/(\d+)\/deliver/) && method === 'POST') {
    return ok(null);
  }

  // ── PAYMENT ──
  if (url.includes('/api/v1/payments/create') && method === 'POST') {
    return ok({ paymentUrl: 'http://localhost:4200/checkout/result' });
  }

  // ── PROMOTION ──
  if (url.includes('/api/v1/vouchers/validate') && method === 'POST') {
    const d = body as VoucherBody;
    if (d.code === 'WELCOME10') {
      return ok({ valid: true, code: 'WELCOME10', discount: 500000, type: 'FIXED', minOrder: 1000000 });
    }
    return ok({ valid: false, message: 'Mã giảm giá không hợp lệ' });
  }

  if (url.includes('/api/v1/vouchers') && method === 'GET') {
    return ok({ content: MOCK_VOUCHERS, page: 0, size: 20, totalElements: MOCK_VOUCHERS.length, totalPages: 1 });
  }

  if (url.includes('/api/v1/vouchers') && method === 'POST') {
    return ok({ id: Date.now(), ...(body as any) });
  }

  if (url.match(/\/api\/v1\/vouchers\/(\d+)/) && method === 'DELETE') {
    return ok(null);
  }

  // ── REVIEW ──
  if (url.includes('/api/v1/reviews') && method === 'POST') {
    return ok({ id: Date.now(), ...(body as any), createdAt: new Date().toISOString() });
  }

  // ── INVENTORY ──
  if (url.match(/\/api\/v1\/inventory\/(\d+)\/transactions/) && method === 'GET') {
    const id = Number(url.match(/\/api\/v1\/inventory\/(\d+)\/transactions/)![1]);
    const content = [
      { id: id * 10 + 1, type: 'IN', quantity: 20, note: 'Nhập hàng đầu kỳ', createdAt: new Date(Date.now() - 86400000 * 20).toISOString(), actorName: 'Admin DAWN' },
      { id: id * 10 + 2, type: 'OUT', quantity: -3, note: 'Xuất theo đơn hàng', createdAt: new Date(Date.now() - 86400000 * 10).toISOString(), actorName: 'Hệ thống' },
      { id: id * 10 + 3, type: 'IN', quantity: 10, note: 'Nhập bổ sung', createdAt: new Date(Date.now() - 86400000 * 3).toISOString(), actorName: 'Nguyễn Văn Staff' },
    ];
    return ok({ content, page: 0, size: 20, totalElements: content.length, totalPages: 1 });
  }

  if (url.match(/\/api\/v1\/inventory\/(\d+)\/restock/) && (method === 'POST' || method === 'PUT')) {
    return ok({ success: true });
  }

  if (url.match(/\/api\/v1\/inventory\/(\d+)/) && method === 'GET') {
    const products = generateProducts();
    return ok(products[0] ? { skuId: Number(url.match(/\/api\/v1\/inventory\/(\d+)/)![1]), stock: 50 } : null);
  }

  if (url.includes('/api/v1/inventory/low-stock') && method === 'GET') {
    return ok(MOCK_LOW_STOCK_ITEMS);
  }

  if (url.includes('/api/v1/inventory') && method === 'GET') {
    const products = generateProducts();
    const params = parseParams(req);
    let filtered = [...products];
    if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
    if (params['brand']) filtered = filtered.filter(p => p.brandName.toLowerCase() === params['brand']!.toLowerCase());
    if (params['lowStock'] === 'true') filtered = filtered.filter(() => Math.random() > 0.7);
    const page = params['page'] ? Number(params['page']) : 0;
    const size = params['size'] ? Number(params['size']) : 20;
    const start = page * size;
    const paged = filtered.slice(start, start + size).map(p => {
      const stockQuantity = p.stockAvailable || Math.floor(Math.random() * 100) + 5;
      const reservedQuantity = Math.floor(Math.random() * 6);
      return {
        skuId: p.id, skuName: p.name, imageUrl: p.imageUrls[0], brandName: p.brandName, price: p.price,
        stockQuantity, reservedQuantity, availableQuantity: stockQuantity - reservedQuantity,
      };
    });
    return ok({ content: paged, page, size, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size) });
  }

  // ── ADMIN DASHBOARD (mock-only, no BE equivalent) ──
  if (url.includes('/api/v1/admin/dashboard/summary') && method === 'GET') {
    return ok(MOCK_DASHBOARD_SUMMARY);
  }
  if (url.includes('/api/v1/admin/dashboard/revenue') && method === 'GET') {
    return ok(MOCK_REVENUE_7_DAYS);
  }

  // ── ADMIN PRODUCTS (mock-only, no BE equivalent) ──
  if (url.match(/\/api\/v1\/admin\/products\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/admin\/products\/(\d+)/)![1]);
    const idx = adminProducts.findIndex(p => p.id === id);
    if (idx >= 0) adminProducts[idx] = { ...adminProducts[idx], ...(body as Partial<Sku>) };
    return idx >= 0 ? ok(adminProducts[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/admin\/products\/(\d+)/) && method === 'DELETE') {
    const id = Number(url.match(/\/api\/v1\/admin\/products\/(\d+)/)![1]);
    adminProducts = adminProducts.filter(p => p.id !== id);
    return ok(null);
  }
  if (url.includes('/api/v1/admin/products') && method === 'POST') {
    const d = body as Partial<Sku>;
    const created: Sku = {
      id: ++nextAdminId, name: d.name || '', slug: (d.name || '').toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      price: d.price || 0, originalPrice: d.originalPrice || undefined,
      imageUrls: d.imageUrls?.length ? d.imageUrls : [`${baseUrl}/400x400/1e293b/white?text=New`],
      description: d.description || undefined,
      brandName: d.brandName || '', status: d.status || 'ACTIVE', soldCount: 0, categoryName: d.categoryName,
      stockAvailable: d.stockAvailable ?? 0,
    };
    adminProducts = [created, ...adminProducts];
    return ok(created);
  }
  if (url.includes('/api/v1/admin/products') && method === 'GET') {
    const params = parseParams(req);
    let filtered = adminProducts;
    if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
    if (params['category']) filtered = filtered.filter(p => p.categoryName === params['category']);
    if (params['brand']) filtered = filtered.filter(p => p.brandName === params['brand']);
    if (params['status']) filtered = filtered.filter(p => p.status === params['status']);
    const page = params['page'] ? Number(params['page']) : 0;
    const size = params['size'] ? Number(params['size']) : 20;
    const start = page * size;
    const paged = filtered.slice(start, start + size);
    return ok({ content: paged, page, size, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size) });
  }

  // ── ADMIN CATEGORIES (mock-only, no BE equivalent) ──
  if (url.match(/\/api\/v1\/admin\/categories\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/admin\/categories\/(\d+)/)![1]);
    const idx = adminCategories.findIndex(c => c.id === id);
    if (idx >= 0) adminCategories[idx] = { ...adminCategories[idx], ...(body as Partial<Category>) };
    return idx >= 0 ? ok(adminCategories[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/admin\/categories\/(\d+)/) && method === 'DELETE') {
    const id = Number(url.match(/\/api\/v1\/admin\/categories\/(\d+)/)![1]);
    adminCategories = adminCategories.filter(c => c.id !== id);
    return ok(null);
  }
  if (url.includes('/api/v1/admin/categories') && method === 'POST') {
    const d = body as Partial<Category>;
    const created: Category = {
      id: ++nextAdminId, name: d.name || '', slug: (d.name || '').toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      parentId: d.parentId ?? null, imageUrl: d.imageUrl, icon: d.icon || 'appstore',
    };
    adminCategories = [created, ...adminCategories];
    return ok(created);
  }
  if (url.includes('/api/v1/admin/categories') && method === 'GET') {
    return ok({ content: adminCategories, page: 0, size: adminCategories.length, totalElements: adminCategories.length, totalPages: 1 });
  }

  // ── ADMIN USERS (mock-only, no BE equivalent) ──
  if (url.match(/\/api\/v1\/admin\/users\/(\d+)\/status/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/admin\/users\/(\d+)\/status/)![1]);
    const idx = adminUsers.findIndex(u => u.id === id);
    if (idx >= 0) adminUsers[idx] = { ...adminUsers[idx], status: (body as { status: 'ACTIVE' | 'INACTIVE' }).status };
    return idx >= 0 ? ok(adminUsers[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/admin\/users\/(\d+)\/reset-password/) && method === 'POST') {
    const tempPassword = Math.random().toString(36).slice(-8);
    return ok({ tempPassword });
  }
  if (url.match(/\/api\/v1\/admin\/users\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/v1\/admin\/users\/(\d+)/)![1]);
    const idx = adminUsers.findIndex(u => u.id === id);
    if (idx >= 0) adminUsers[idx] = { ...adminUsers[idx], ...(body as Partial<User>) };
    return idx >= 0 ? ok(adminUsers[idx]) : err(404, 'Not found');
  }
  if (url.includes('/api/v1/admin/users') && method === 'POST') {
    const d = body as Partial<User>;
    const created: User = {
      id: ++nextAdminId, fullName: d.fullName || '', email: d.email || '', phone: d.phone,
      systemRole: d.systemRole || 'CUSTOMER', status: 'ACTIVE', createdAt: new Date().toISOString(),
    };
    adminUsers = [created, ...adminUsers];
    return ok(created);
  }
  if (url.includes('/api/v1/admin/users') && method === 'GET') {
    const params = parseParams(req);
    let filtered = adminUsers;
    if (params['role']) filtered = filtered.filter(u => u.systemRole === params['role']);
    if (params['search']) filtered = filtered.filter(u => u.fullName.toLowerCase().includes(params['search'].toLowerCase()) || u.email.toLowerCase().includes(params['search'].toLowerCase()));
    const page = params['page'] ? Number(params['page']) : 0;
    const size = params['size'] ? Number(params['size']) : 20;
    const start = page * size;
    const paged = filtered.slice(start, start + size);
    return ok({ content: paged, page, size, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size) });
  }

  // ── ADMIN RETURNS (mock-only, no BE equivalent) ──
  if (url.match(/\/api\/v1\/admin\/returns\/(\d+)\/approve/) && method === 'POST') {
    const id = Number(url.match(/\/api\/v1\/admin\/returns\/(\d+)\/approve/)![1]);
    const idx = returnRequests.findIndex(r => r.id === id);
    if (idx >= 0) {
      returnRequests[idx] = {
        ...returnRequests[idx], status: 'APPROVED',
        approvedBy: authService.currentUser()?.fullName || 'Admin', approvedAt: new Date().toISOString(),
      };
    }
    return idx >= 0 ? ok(returnRequests[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/admin\/returns\/(\d+)\/reject/) && method === 'POST') {
    const id = Number(url.match(/\/api\/v1\/admin\/returns\/(\d+)\/reject/)![1]);
    const idx = returnRequests.findIndex(r => r.id === id);
    if (idx >= 0) {
      returnRequests[idx] = {
        ...returnRequests[idx], status: 'REJECTED', rejectReason: (body as { reason: string }).reason,
        approvedBy: authService.currentUser()?.fullName || 'Admin', approvedAt: new Date().toISOString(),
      };
    }
    return idx >= 0 ? ok(returnRequests[idx]) : err(404, 'Not found');
  }
  if (url.match(/\/api\/v1\/admin\/returns\/(\d+)/) && method === 'GET') {
    const id = Number(url.match(/\/api\/v1\/admin\/returns\/(\d+)/)![1]);
    const found = returnRequests.find(r => r.id === id) || null;
    return found ? ok(found) : err(404, 'Not found');
  }
  if (url.includes('/api/v1/admin/returns') && method === 'GET') {
    const params = parseParams(req);
    let filtered = returnRequests;
    if (params['status']) filtered = filtered.filter(r => r.status === params['status']);
    return ok({ content: filtered, page: 0, size: filtered.length, totalElements: filtered.length, totalPages: 1 });
  }

  // ── ADMIN REVIEWS (mock-only, no BE equivalent) ──
  if (url.match(/\/api\/v1\/admin\/reviews\/(\d+)\/moderate/) && method === 'POST') {
    const id = Number(url.match(/\/api\/v1\/admin\/reviews\/(\d+)\/moderate/)![1]);
    const idx = reviewQueue.findIndex(r => r.id === id);
    if (idx >= 0) reviewQueue[idx] = { ...reviewQueue[idx], status: (body as { status: 'APPROVED' | 'HIDDEN' }).status };
    return idx >= 0 ? ok(reviewQueue[idx]) : err(404, 'Not found');
  }
  if (url.includes('/api/v1/admin/reviews') && method === 'GET') {
    const params = parseParams(req);
    let filtered = reviewQueue;
    if (params['status']) filtered = filtered.filter(r => r.status === params['status']);
    return ok({ content: filtered, page: 0, size: filtered.length, totalElements: filtered.length, totalPages: 1 });
  }

  // ── ADMIN AUDIT LOGS (mock-only, no BE equivalent) ──
  if (url.includes('/api/v1/admin/audit-logs') && method === 'GET') {
    const params = parseParams(req);
    let filtered = MOCK_AUDIT_LOGS;
    if (params['action']) filtered = filtered.filter(a => a.action.toLowerCase().includes(params['action'].toLowerCase()));
    if (params['entityType']) filtered = filtered.filter(a => a.entityType === params['entityType']);
    if (params['outcome']) filtered = filtered.filter(a => a.outcome === params['outcome']);
    return ok({ content: filtered, page: 0, size: filtered.length, totalElements: filtered.length, totalPages: 1 });
  }

  // ── BUILDER (mock-only, no BE equivalent) ──
  if (url.includes('/api/v1/builder/slots') && method === 'GET') {
    return ok({ content: MOCK_BUILDER_SLOTS });
  }
  if (url.includes('/api/v1/builder/validate') && method === 'POST') {
    return ok({ valid: true, conflicts: [], warnings: [] });
  }
  if (url.includes('/api/v1/builder/templates') && method === 'GET') {
    return ok({
      content: MOCK_BUILDER_TEMPLATES,
      page: 0, size: 12, totalElements: MOCK_BUILDER_TEMPLATES.length, totalPages: 1,
    });
  }

  // ── PASS THROUGH ──
  
  if (url.includes('/api/v1/wishlist') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/wishlist') && (method === 'POST' || method === 'DELETE')) {
    return ok(null);
  }
  if (url.includes('/api/v1/notifications') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/notifications') && method === 'PUT') {
    return ok(null);
  }
  if (url.includes('/api/v1/promotion/flash-sales/active') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/articles') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/admin/reports/revenue') && method === 'GET') {
    return ok(MOCK_REVENUE_7_DAYS);
  }
  if (url.includes('/api/v1/admin/reports/top-products') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('return-requests') && method === 'POST') {
    return ok({ id: Date.now(), status: 'REQUESTED' });
  }
  
  if (url.includes('/api/v1/admin/purchasing/suppliers') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/admin/purchasing/suppliers') && method === 'POST') {
    return ok({ id: Date.now(), ...(body as any) });
  }
  if (url.includes('/api/v1/admin/purchasing/orders') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/admin/purchasing/orders') && method === 'POST') {
    return ok({ id: Date.now(), status: 'DRAFT', ...(body as any) });
  }
  if (url.includes('/api/v1/admin/stock-takes') && method === 'POST') {
    return ok({ id: Date.now(), status: 'IN_PROGRESS' });
  }
  if (url.includes('/api/v1/admin/stock-takes') && method === 'GET') {
    return ok([]);
  }
  if (url.includes('/api/v1/admin/reports/export') && method === 'GET') {
    return ok('day,revenue');
  }
  return next(req);
};
