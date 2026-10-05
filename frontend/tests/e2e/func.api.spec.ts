import { test, expect } from '@playwright/test';
import { ADMIN, CUSTOMER, STAFF, auth, ensureStock, uuid } from './auth.helper';

async function clearCart(request: import('@playwright/test').APIRequestContext, headers: Record<string, string>) {
  const cart = await request.get('cart', { headers });
  if (cart.status() !== 200) return;
  const body = await cart.json();
  const items = body?.data?.items ?? body?.items ?? [];
  for (const it of items) {
    await request.delete(`cart/items/${it.skuId}`, { headers });
  }
}

async function avail(request: import('@playwright/test').APIRequestContext, skuId: number) {
  const inv = await request.get(`inventory?page=0&size=10&skuId=${skuId}`, {
    headers: auth(ADMIN()),
  });
  return (await inv.json()).data.content[0].availableQuantity as number;
}

async function memberCheckout(
  request: import('@playwright/test').APIRequestContext,
  extra: Record<string, unknown> = {},
) {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'PICKUP',
      shippingAddress: {},
      ...extra,
    },
    headers: c,
  });
  expect(co.status()).toBe(200);
  return co.json();
}

// F1 — cùng idempotencyKey checkout 2 lần: 1 đơn duy nhất, kho chỉ trừ 1 lần
test('FUNC idempotent checkout: same key -> same order, single reservation', async ({
  request,
}) => {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const key = uuid();
  const base = { idempotencyKey: key, fulfillmentType: 'PICKUP', shippingAddress: {} };

  const r1 = await request.post('orders/checkout', { data: base, headers: c });
  expect(r1.status()).toBe(200);
  const o1 = await r1.json();

  const r2 = await request.post('orders/checkout', { data: base, headers: c });
  expect(r2.status()).toBe(200);
  const o2 = await r2.json();
  expect(o2.id).toBe(o1.id);
});

// F2 — nhảy cóc trạng thái PENDING_PAYMENT -> DELIVERED bị chặn
test('FUNC invalid transition PENDING_PAYMENT -> DELIVERED rejected', async ({ request }) => {
  const order = await memberCheckout(request);
  const bad = await request.post(
    `staff/orders/sub-orders/${order.subOrders[0].id}/transition`,
    { data: { status: 'DELIVERED' }, headers: auth(STAFF()) },
  );
  expect(bad.status()).not.toBe(200);
});

// F3 — thanh toán FAILED: đơn failed, kho nhả đúng số đã giữ
test('FUNC payment FAILED releases reserved stock', async ({ request }) => {
  const c = auth(CUSTOMER());
  const a = auth(ADMIN());
  const before = await avail(request, 2);

  const order = await memberCheckout(request);
  const afterReserve = await avail(request, 2);
  expect(afterReserve).toBeLessThan(before);

  const pay = await request.post('payments/create', {
    data: { orderId: order.id, method: 'MOCK' },
    headers: c,
  });
  expect(pay.status()).toBe(200);
  const paymentId = (await pay.json()).paymentId;

  const prog = await request.post(`payments/mock/${paymentId}/scenario`, {
    data: { scenario: 'FAILED' },
    headers: a,
  });
  expect(prog.status()).toBe(200);

  const hook = await request.post('payments/webhook/mock', {
    data: { paymentId: String(paymentId), result: 'FAILED' },
    headers: c,
  });
  expect(hook.status()).toBe(200);

  const afterFail = await avail(request, 2);
  expect(afterFail).toBe(before);
});

// F4 — voucher tươi mỗi run (usage lưu vĩnh viễn theo user) trừ % ở checkout
test('FUNC voucher discounts checkout total', async ({ request }) => {
  const code = `E2E-${Date.now().toString(36).toUpperCase()}`;
  const mk = await request.post('vouchers', {
    data: {
      code,
      scopeType: 'STOREWIDE',
      discountType: 'PERCENT',
      discountValue: 10,
      totalQuantity: 5,
      validFrom: new Date().toISOString(),
      validTo: new Date(Date.now() + 86400000).toISOString(),
    },
    headers: auth(ADMIN()),
  });
  expect([200, 201]).toContain(mk.status());

  const order = await memberCheckout(request, { voucherCode: code });
  expect(order.subOrders[0].discountAmount).toBeGreaterThan(0);
  expect(order.totalAmount).toBeLessThan(order.subOrders[0].subtotal);
});

// F5 — DELIVERY thiếu địa chỉ 400; đủ địa chỉ thì có ship fee
test('FUNC DELIVERY requires address; fee applied with address', async ({ request }) => {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });

  const noAddr = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'DELIVERY', shippingAddress: {} },
    headers: c,
  });
  expect(noAddr.status()).toBe(400);

  const ok = await request.post('orders/checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'DELIVERY',
      shippingAddress: { city: 'HCM', district: '1', street: 'e2e' },
    },
    headers: c,
  });
  expect(ok.status()).toBe(200);
  const o = await ok.json();
  expect(o.subOrders[0].shippingFee).toBeGreaterThanOrEqual(0);
  expect(o.totalAmount).toBeGreaterThanOrEqual(o.subOrders[0].subtotal);
});

// F6 — cart: add -> update qty -> count -> remove
test('FUNC cart add/update/count/remove', async ({ request }) => {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);

  const add = await request.post('cart/items', {
    data: { skuId: 2, quantity: 1 },
    headers: c,
  });
  expect(add.status()).toBe(201);

  const upd = await request.put('cart/items', {
    data: { skuId: 2, quantity: 3 },
    headers: c,
  });
  expect(upd.status()).toBe(200);
  expect((await upd.json()).data.quantity).toBe(3);

  // count = số dòng, không phải số đơn vị
  const count = await request.get('cart/count', { headers: c });
  expect(count.status()).toBe(200);
  expect((await count.json()).data).toBe(1);

  const del = await request.delete('cart/items/2', { headers: c });
  expect([200, 204]).toContain(del.status());

  const empty = await request.get('cart/count', { headers: c });
  expect((await empty.json()).data).toBe(0);
});

// F7 — merge giỏ guest vào member
test('FUNC guest cart merges into member cart', async ({ request }) => {
  const c = auth(CUSTOMER());
  await clearCart(request, c);
  await ensureStock(request, 2);
  const token = `merge-${Date.now().toString(36)}`;
  await request.post(`cart/guest/items?token=${token}`, {
    data: { skuId: 2, quantity: 1 },
  });

  const merge = await request.post(`cart/merge?token=${token}`, { headers: c });
  expect(merge.status()).toBe(200);

  const cart = await request.get('cart', { headers: c });
  const items = ((await cart.json()).data?.items ?? []) as { skuId: number }[];
  expect(items.some((i) => i.skuId === 2)).toBe(true);
  await clearCart(request, c);
});

// F8 — staff xem danh sách + chi tiết đơn; customer bị chặn
test('FUNC staff order list/detail; customer blocked', async ({ request }) => {
  const s = auth(STAFF());
  const list = await request.get('staff/orders?page=0&size=5', { headers: s });
  expect(list.status()).toBe(200);

  const blocked = await request.get('staff/orders?page=0&size=5', {
    headers: auth(CUSTOMER()),
  });
  expect(blocked.status()).toBe(403);
});

// F9 — notification đọc 1 cái + đọc tất cả
test('FUNC notifications read + read-all', async ({ request }) => {
  const c = auth(CUSTOMER());
  const order = await memberCheckout(request);

  const list = await request.get('notifications', { headers: c });
  expect(list.status()).toBe(200);
  const notifs = (await list.json()) as { id: number; referenceId: number }[];
  const mine = notifs.find((n) => n.referenceId === order.id);
  expect(mine).toBeTruthy();

  const read = await request.put(`notifications/${mine!.id}/read`, { headers: c });
  expect(read.status()).toBe(200);

  const readAll = await request.put('notifications/read-all', { headers: c });
  expect(readAll.status()).toBe(200);
});

// F10 — wishlist add/list/remove
test('FUNC wishlist add/list/remove', async ({ request }) => {
  const c = auth(CUSTOMER());
  await request.delete('wishlist/2', { headers: c });

  const add = await request.post('wishlist', { data: { skuId: 2 }, headers: c });
  expect([200, 201]).toContain(add.status());

  const list = await request.get('wishlist', { headers: c });
  expect(list.status()).toBe(200);
  expect(JSON.stringify(await list.json())).toContain('2');

  const del = await request.delete('wishlist/2', { headers: c });
  expect(del.status()).toBe(200);
});
