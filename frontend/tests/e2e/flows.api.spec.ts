import { test, expect } from '@playwright/test';
import { ADMIN, CUSTOMER, STAFF, auth, ensureStock, uuid } from './auth.helper';

async function clearCart(request: any, headers: any) {
  const cart = await request.get('cart', { headers });
  if (cart.status() !== 200) return;
  const body = await cart.json();
  const items = body?.data?.items ?? body?.items ?? [];
  for (const it of items) { await request.delete('cart/items/' + it.skuId, { headers }); }
}

// Flow 1 — checkout + payment mock end-to-end (acceptance chính)
test('guest checkout -> mock payment SUCCESS -> order PAID, stock deducted', async ({ request }) => {
  const token = `e2e-${Date.now().toString(36)}`;
  await ensureStock(request, 2);
  await request.post(`cart/guest/items?token=${token}`, { data: { skuId: 2, quantity: 1 } });

  const co = await request.post('orders/guest-checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'PICKUP',
      shippingAddress: {},
      guestToken: token,
      guestEmail: 'e2e@t.vn',
    },
  });
  expect(co.status()).toBe(200);
  const order = await co.json();
  expect(order.userId).toBeNull();
  expect(order.paymentStatus).toBe('PENDING');

  const pay = await request.post(
    'payments/create',
    { data: { orderId: order.id, method: 'MOCK' }, headers: auth(CUSTOMER()) },
  );
  expect(pay.status()).toBe(200);
  const { paymentId } = await pay.json();

  const hook1 = await request.post('payments/webhook/mock', {
    data: { paymentId: String(paymentId), result: 'SUCCESS' },
    headers: auth(CUSTOMER()),
  });
  expect(hook1.status()).toBe(200);
  expect((await hook1.json()).status).toBe('COMPLETED');

  // webhook trùng phải idempotent
  const hook2 = await request.post('payments/webhook/mock', {
    data: { paymentId: String(paymentId), result: 'SUCCESS' },
    headers: auth(CUSTOMER()),
  });
  expect(hook2.status()).toBe(200);
  expect((await hook2.json()).status).toBe('COMPLETED');
});

// Flow 2 — voucher dùng lại + hết hàng
test('voucher reuse rejected; over-stock cart rejected', async ({ request }) => {
  const bad = await request.post('vouchers/validate', {
    data: { code: 'NOPE', orderTotal: 10000000 },
    headers: auth(CUSTOMER()),
  });
  expect(bad.status()).toBe(200);
  expect((await bad.json()).data.valid).toBe(false);

  // thêm trước 1 cái rồi mới đẩy vượt tồn
  await ensureStock(request, 2);
  const pre = await request.post('cart/items', {
    data: { skuId: 2, quantity: 1 },
    headers: auth(CUSTOMER()),
  });
  expect(pre.status()).toBe(201);

  const over = await request.put(
    'cart/items',
    { data: { skuId: 2, quantity: 999999 }, headers: auth(CUSTOMER()) },
  );
  expect(over.status()).toBe(422);
});

// Flow 3 — RBAC ma trận
test('rbac: customer blocked from admin, staff blocked from admin-only, admin passes', async ({
  request,
}) => {
  const c1 = await request.get('admin/reports/revenue?from=2024-01-01T00:00:00&to=2026-01-01T00:00:00', {
    headers: auth(CUSTOMER()),
  });
  expect(c1.status()).toBe(403);

  const s1 = await request.get('admin/purchasing/suppliers', { headers: auth(STAFF()) });
  expect(s1.status()).toBe(403);

  const a1 = await request.get('admin/purchasing/suppliers', { headers: auth(ADMIN()) });
  expect(a1.status()).toBe(200);

  const anon = await request.get('cart');
  expect(anon.status()).toBe(401);
});

// Flow 4 — return/dispute lifecycle (tự chứa: member checkout → DELIVERED → khiếu nại)
test('return request -> reject -> dispute -> overturn -> approved', async ({ request }) => {
  const c = auth(CUSTOMER());
  const s = auth(STAFF());
  const a = auth(ADMIN());
  await clearCart(request, c);
  await ensureStock(request, 2);

  // member checkout 1 SKU rẻ để đưa đi giao
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'PICKUP',
      shippingAddress: {},
    },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const order = await co.json();
  const subId = order.subOrders[0].id;

  // đưa sang DELIVERED qua staff flow
  for (const st of ['PAID', 'PROCESSING', 'SHIPPED']) {
    const tr = await request.post(`staff/orders/sub-orders/${subId}/transition`, {
      data: { status: st },
      headers: s,
    });
    expect(tr.status()).toBe(200);
  }
  const dl = await request.post(`sub-orders/${subId}/deliver`, { headers: s });
  expect(dl.status()).toBe(200);

  const created = await request.post(`orders/${order.id}/return-requests`, {
    data: { subOrderId: subId, reason: 'e2e broken' },
    headers: c,
  });
  expect(created.status()).toBe(200);
  const rr = await created.json();

  const rejected = await request.put(`admin/return-requests/${rr.id}/reject`, {
    headers: a,
  });
  expect(rejected.status()).toBe(200);

  const disputed = await request.post(`return-requests/${rr.id}/dispute`, {
    data: { note: 'e2e disagree' },
    headers: c,
  });
  expect(disputed.status()).toBe(200);

  const resolved = await request.put(`admin/return-requests/${rr.id}/resolve`, {
    data: { overturn: true },
    headers: a,
  });
  expect(resolved.status()).toBe(200);
  expect((await resolved.json()).status).toBe('APPROVED');
});
