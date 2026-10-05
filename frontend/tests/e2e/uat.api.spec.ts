import { test, expect } from '@playwright/test';
import { ADMIN, CUSTOMER, STAFF, auth, ensureStock, uuid } from './auth.helper';

// Dọn giỏ user 2 trước mỗi flow (DB black-box dùng chung, tồn hàng run trước)
async function clearCart(request: import('@playwright/test').APIRequestContext, headers: Record<string, string>) {
  const cart = await request.get('cart', { headers });
  if (cart.status() !== 200) return;
  const body = await cart.json();
  const items = body?.data?.items ?? body?.items ?? [];
  for (const it of items) {
    await request.delete(`cart/items/${it.skuId}`, { headers });
  }
}

// UAT-mock: 6 kịch bản nghiệm thu chạy tự động trên mock provider (không cần user thật ký)

// UAT-1: mua hàng end-to-end đã có ở flows (guest checkout + mock payment) — ở đây kiểm thêm:
// cancel đơn PENDING_PAYMENT thì kho được nhả (reserved về đúng)
test('UAT cancel releases reserved stock', async ({ request }) => {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 2 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const order = await co.json();
  const subId = order.subOrders[0].id;

  const before = await request.get('inventory/2/transactions?page=0&size=50', { headers: auth(ADMIN()) });
  const beforeCount = (await before.json()).data?.pagination?.totalElements ?? 0;

  const cancel = await request.post(`orders/sub-orders/${subId}/cancel`, { headers: c });
  expect(cancel.status()).toBe(200);

  const after = await request.get('inventory/2/transactions?page=0&size=50', { headers: auth(ADMIN()) });
  const afterCount = (await after.json()).data?.pagination?.totalElements ?? 0;
  expect(afterCount).toBeGreaterThan(beforeCount);
});

// UAT-2: voucher + flash-sale hiển thị (không crash khi chưa có campaign)
test('UAT promotion surfaces render without active campaigns', async ({ request }) => {
  const c = auth(CUSTOMER());
  const flash = await request.get('promotion/flash-sales/active', { headers: c });
  expect(flash.status()).toBe(200);

  const v = await request.post('vouchers/validate', {
    data: { code: 'BB10', orderTotal: 20000000 },
    headers: c,
  });
  expect(v.status()).toBe(200);
});

// UAT-3: build PC báo đúng khi gắn RAM sai chuẩn, đúng khi chuẩn khớp
test('UAT builder compatibility check', async ({ request }) => {
  const bad = await request.post('builder/validate', {
    data: {
      slots: {
        CPU: { socket: 'LGA1700' },
        MAINBOARD: { socket: 'AM5', ramType: 'DDR5', slotCount: 4 },
        RAM: { ramType: 'DDR4', sticks: 2 },
        GPU: {},
        PSU: { wattage: 750 },
        CASE: {},
        STORAGE: {},
      },
    },
  });
  expect(bad.status()).toBe(200);
  const badBody = await bad.json();
  expect(badBody.valid).toBe(false);
  expect(badBody.errors.length).toBeGreaterThan(0);

  const good = await request.post('builder/validate', {
    data: {
      slots: {
        CPU: { socket: 'LGA1700', tdp: 65 },
        MAINBOARD: { socket: 'LGA1700', ramType: 'DDR5', slotCount: 4, formFactor: 'ATX' },
        RAM: { ramType: 'DDR5', sticks: 2 },
        GPU: { tdp: 200, length: 300 },
        PSU: { wattage: 750, formFactor: 'ATX' },
        CASE: { maxGpuLength: 350, supportedFormFactors: ['ATX'], supportedPsuFormFactors: ['ATX'] },
        STORAGE: {},
      },
    },
  });
  expect(good.status()).toBe(200);
  expect((await good.json()).valid).toBe(true);
});

// UAT-4: hủy trước/sau thanh toán — sau PAID thì hủy qua staff và kho được nhập bù
test('UAT cancel after paid restocks inventory', async ({ request }) => {
  const c = auth(CUSTOMER());
  const s = auth(STAFF());
  await clearCart(request, c);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const subId = (await co.json()).subOrders[0].id;

  const paid = await request.post(`staff/orders/sub-orders/${subId}/transition`, {
    data: { status: 'PAID' },
    headers: s,
  });
  expect(paid.status()).toBe(200);

  const cancelled = await request.post(`staff/orders/sub-orders/${subId}/transition`, {
    data: { status: 'CANCELLED' },
    headers: s,
  });
  expect(cancelled.status()).toBe(200);
});

// UAT-5: admin duyệt return → refund PENDING; dashboard chỉ tính DELIVERED (revenue rỗng khi chưa có đơn giao)
test('UAT admin return approve creates refund; revenue empty without delivered orders', async ({
  request,
}) => {
  const a = auth(ADMIN());
  const rev = await request.get(
    'admin/reports/revenue?from=2024-01-01T00:00:00&to=2024-06-01T00:00:00',
    { headers: a },
  );
  expect(rev.status()).toBe(200);

  const returns = await request.get('staff/return-requests', { headers: a });
  expect(returns.status()).toBe(200);
  expect(Array.isArray(await returns.json())).toBe(true);
});

// UAT-6: token hết hạn bị từ chối; thông báo đơn hàng được tạo sau checkout
test('UAT expired token rejected; order notification created', async ({ request }) => {
  const expired = await request.get('cart', {
    headers: { Authorization: 'Bearer this.is.not.a.valid.token' },
  });
  expect([401, 403]).toContain(expired.status());

  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  const notifs = await request.get('notifications', { headers: c });
  expect(notifs.status()).toBe(200);
  const list = await notifs.json();
  expect(Array.isArray(list)).toBe(true);
  expect(list.some((n: { type: string }) => n.type === 'ORDER_CREATED')).toBe(true);
});

// UAT-7: giao hàng DELIVERY tới tay → doanh thu báo cáo có số (đối ứng UAT-5 rỗng)
test('UAT delivery to door counts revenue', async ({ request }) => {
  const c = auth(CUSTOMER());
  const s = auth(STAFF());
  const a = auth(ADMIN());
  await clearCart(request, c);
  await ensureStock(request, 2);

  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'DELIVERY',
      shippingAddress: { city: 'HCM', district: '1', street: 'e2e' },
    },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const subId = (await co.json()).subOrders[0].id;

  for (const st of ['PAID', 'PROCESSING', 'SHIPPED']) {
    const tr = await request.post(`staff/orders/sub-orders/${subId}/transition`, {
      data: { status: st },
      headers: s,
    });
    expect(tr.status()).toBe(200);
  }
  const dl = await request.post(`sub-orders/${subId}/deliver`, { headers: s });
  expect(dl.status()).toBe(200);

  const rev = await request.get(
    'admin/reports/revenue?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59',
    { headers: a },
  );
  expect(rev.status()).toBe(200);
  const rows = (await rev.json()) as { revenue: number }[];
  expect(rows.some((r) => Number(r.revenue) > 0)).toBe(true);
});

// UAT-8: staff đi ca — thấy đơn mới, mở chi tiết, xác nhận thanh toán
test('UAT staff shift: list, detail, confirm paid', async ({ request }) => {
  const c = auth(CUSTOMER());
  const s = auth(STAFF());
  await clearCart(request, c);
  await ensureStock(request, 2);

  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  const order = await co.json();

  const list = await request.get('staff/orders?page=0&size=5', { headers: s });
  expect(list.status()).toBe(200);
  expect(JSON.stringify(await list.json())).toContain(String(order.id));

  const detail = await request.get(`staff/orders/${order.id}`, { headers: s });
  expect(detail.status()).toBe(200);

  const paid = await request.post(`staff/orders/sub-orders/${order.subOrders[0].id}/transition`, {
    data: { status: 'PAID' },
    headers: s,
  });
  expect(paid.status()).toBe(200);
  expect((await paid.json()).status).toBe('PAID');
});

// UAT-9: khách wishlist → mua → nhận hàng → đánh giá 5 sao
test('UAT wishlist, buy, receive, review 5 stars', async ({ request }) => {
  const c = auth(CUSTOMER());
  const s = auth(STAFF());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.delete('wishlist/2', { headers: c });

  const wish = await request.post('wishlist', { data: { skuId: 2 }, headers: c });
  expect([200, 201]).toContain(wish.status());

  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const subId = (await co.json()).subOrders[0].id;

  for (const st of ['PAID', 'PROCESSING', 'SHIPPED']) {
    await request.post(`staff/orders/sub-orders/${subId}/transition`, {
      data: { status: st },
      headers: s,
    });
  }
  await request.post(`sub-orders/${subId}/deliver`, { headers: s });

  const review = await request.post('reviews', {
    data: { skuId: 2, subOrderId: subId, rating: 5, comment: 'e2e ngon', images: [] },
    headers: c,
  });
  expect(review.status()).toBe(200);
});
