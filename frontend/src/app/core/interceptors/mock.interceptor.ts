import { HttpRequest, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, delay } from 'rxjs';
import { PRODUCT_TEMPLATES } from '@core/mocks/product-template.mock';
import { MOCK_CATEGORIES, MOCK_BRANDS } from '@core/mocks/category.mock';
import { MOCK_REVIEWS } from '@core/mocks/review.mock';
import { MOCK_ORDERS } from '@core/mocks/order.mock';
import { MOCK_FLASH_SALE_CAMPAIGN } from '@core/mocks/flash-sale.mock';
import { MOCK_DASHBOARD_SUMMARY, MOCK_REVENUE_7_DAYS, MOCK_LOW_STOCK_ITEMS } from '@core/mocks/dashboard.mock';
import { MOCK_VOUCHERS } from '@core/mocks/voucher.mock';
import { MOCK_BUILDER_SLOTS, MOCK_BUILDER_TEMPLATES } from '@core/mocks/builder.mock';

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
let nextOrderId = 2000;

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
  const { method, url, body } = req;

  // ── AUTH ──
  if (url.includes('/api/auth/refresh') && method === 'POST') {
    return ok(mockUserSession('user@dawn.vn', 'User', 'CUSTOMER'));
  }

  // ── CATALOG ──
  if (url.includes('/api/catalog/categories') && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_CATEGORIES }));
  }

  if (url.includes('/api/catalog/brands') && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_BRANDS }));
  }

  if (url.match(/\/api\/catalog\/skus\/([^/]+)\/reviews/) && method === 'GET') {
    return ok(MOCK_REVIEWS);
  }

  if (url.match(/\/api\/catalog\/skus\/([^/]+)\/related/) && method === 'GET') {
    const slug = url.split('/').pop()!;
    const all = generateProducts();
    const idx = all.findIndex(p => p.slug === slug);
    return ok(all.filter((_, i) => i !== idx).slice(0, 4));
  }

  if (url.match(/\/api\/catalog\/skus\/([^/]+)/) && method === 'GET') {
    const slug = url.split('/').pop()!;
    const p = generateProducts().find(pr => pr.slug === slug) || null;
    return p ? ok(p) : err(404, 'Not found');
  }

  if (url.includes('/api/catalog/skus') && method === 'GET') {
    let filtered = generateProducts();
    const params = parseParams(req);

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
    return ok({ items: cartItems, version: 1 });
  }

  if (url.includes('/api/cart/items') && method === 'POST') {
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

  if (url.match(/\/api\/cart\/items\/(\d+)/) && method === 'PUT') {
    const id = Number(url.match(/\/api\/cart\/items\/(\d+)/)![1]);
    const d = body as CartUpdateBody;
    const idx = cartItems.findIndex(i => i.skuId === id);
    if (idx >= 0) {
      cartItems[idx] = { ...cartItems[idx], quantity: d.quantity };
    }
    return ok({ items: cartItems, version: 2 });
  }

  if (url.match(/\/api\/cart\/items\/(\d+)/) && method === 'DELETE') {
    const id = Number(url.match(/\/api\/cart\/items\/(\d+)/)![1]);
    cartItems = cartItems.filter(i => i.skuId !== id);
    return ok({ items: cartItems, version: 2 });
  }

  if (url.includes('/api/cart') && method === 'DELETE') {
    cartItems = [];
    return ok({ items: [], version: null });
  }

  // ── ORDERS ──
  if (url.includes('/api/orders/my') && method === 'GET') {
    return ok({ content: localOrders, pagination: { pageNumber: 0, pageSize: 20, totalElements: localOrders.length } });
  }

  if (url.match(/\/api\/orders\/(\d+)\/cancel/) && method === 'PUT') {
    return ok(null);
  }

  if (url.match(/\/api\/orders\/(\d+)\/items\/(\d+)\/reviews/) && method === 'POST') {
    return ok({ id: Date.now(), ...(body as Record<string, unknown>), createdAt: new Date().toISOString() });
  }

  if (url.match(/\/api\/orders\/(\d+)/) && method === 'GET') {
    const id = Number(url.match(/\/api\/orders\/(\d+)/)![1]);
    const order = localOrders.find(o => o.id === id);
    return order ? ok(order) : err(404, 'Not found');
  }

  if (url.includes('/api/orders/checkout') && method === 'POST') {
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

  // ── PAYMENT ──
  if (url.includes('/api/payments/vnpay/initiate') && method === 'POST') {
    return ok({ paymentUrl: 'http://localhost:4201/checkout/result' });
  }

  // ── PROMOTION ──
  if (url.includes('/api/promotion/flash-sales/active') && method === 'GET') {
    return ok(MOCK_FLASH_SALE_CAMPAIGN);
  }

  if (url.includes('/api/promotions/vouchers/validate') && method === 'POST') {
    const d = body as VoucherBody;
    if (d.code === 'WELCOME10') {
      return ok({ valid: true, code: 'WELCOME10', discount: 500000, type: 'FIXED', minOrder: 1000000 });
    }
    return ok({ valid: false, message: 'Mã giảm giá không hợp lệ' });
  }

  if (url.includes('/api/promotions/vouchers') && method === 'GET') {
    return ok(MOCK_VOUCHERS);
  }

  // ── ADMIN ──
  if (url.includes('/api/admin/dashboard/summary') && method === 'GET') {
    return ok(MOCK_DASHBOARD_SUMMARY);
  }

  if (url.includes('/api/admin/orders') && method === 'GET') {
    return ok({ content: localOrders, pagination: { pageNumber: 0, pageSize: 20, totalElements: localOrders.length } });
  }

  if (url.match(/\/api\/admin\/orders\/(\d+)\/status/) && method === 'PUT') {
    return ok(null);
  }

  // ── ADMIN: REVENUE ──
  if (url.includes('/api/admin/dashboard/revenue') && method === 'GET') {
    return ok(MOCK_REVENUE_7_DAYS);
  }

  // ── INVENTORY ──
  if (url.includes('/api/inventory/low-stock') && method === 'GET') {
    return ok(MOCK_LOW_STOCK_ITEMS);
  }

  if (url.includes('/api/inventory') && method === 'GET') {
    const products = generateProducts();
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
    return ok({ content: MOCK_BUILDER_SLOTS });
  }

  if (url.includes('/api/builder/validate') && method === 'POST') {
    return ok({ valid: true, conflicts: [], warnings: [] });
  }

  if (url.includes('/api/builder/templates') && method === 'GET') {
    return ok({
      content: MOCK_BUILDER_TEMPLATES,
      pagination: { pageNumber: 0, pageSize: 12, totalElements: MOCK_BUILDER_TEMPLATES.length },
    });
  }

  // ── PASS THROUGH ──
  return next(req);
};
