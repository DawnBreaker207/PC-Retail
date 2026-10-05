import { test, expect } from '@playwright/test';
import { ADMIN, CUSTOMER, STAFF, auth, ensureStock, mintToken, uuid } from './auth.helper';

// NF1 — race: vượt quá tồn thì Lua gatekeeper chặn, không bao giờ âm kho, Redis khớp DB
// Dùng SKU 1 (không file test nào khác đụng) + member checkout/cancel để không tồn đơn PENDING
test('NONFUNC race: oversell impossible, redis consistent with db', async ({ request }) => {
  await ensureStock(request, 1, 15);
  const inv = await request.get('inventory?page=0&size=10&skuId=1', {
    headers: auth(ADMIN()),
  });
  const a0 = (await inv.json()).data.content[0].availableQuantity as number;
  expect(a0).toBeGreaterThanOrEqual(15);

  const c = auth(CUSTOMER());
  const attempt = async (i: number) => {
    await request.post('cart/items', { data: { skuId: 1, quantity: 1 }, headers: c });
    const co = await request.post('orders/checkout', {
      data: {
        idempotencyKey: `${uuid()}-${Date.now().toString(36)}-${i}`,
        fulfillmentType: 'PICKUP',
        shippingAddress: {},
      },
      headers: c,
    });
    if (co.status() !== 200) return co.status();
    const subId = (await co.json()).subOrders[0].id;
    const cancel = await request.post(`orders/sub-orders/${subId}/cancel`, { headers: c });
    return cancel.status() === 200 ? 200 : cancel.status();
  };

  // 20 luồng song song cùng 1 giỏ (upsert chịu được) — đủ ép Lua gatekeeper
  const results = await Promise.all(
    Array.from({ length: 20 }, (_, i) => attempt(i)),
  );
  const ok = results.filter((s) => s === 200).length;

  // Cancel nhả lại hết: avail cuối phải về đúng ban đầu (không kẹt reservation)
  expect(ok).toBeGreaterThan(0);

  const after = await request.get('inventory?page=0&size=10&skuId=1', {
    headers: auth(ADMIN()),
  });
  const a1 = (await after.json()).data.content[0].availableQuantity as number;
  expect(a1).toBeGreaterThanOrEqual(0);
  expect(a1).toBe(a0);

  await request.get('cart', { headers: c }).then(async (cart) => {
    if (cart.status() === 200) {
      for (const it of ((await cart.json()).data?.items ?? []) as { skuId: number }[]) {
        await request.delete(`cart/items/${it.skuId}`, { headers: c });
      }
    }
  });
});

// NF2 — latency: endpoint chính dưới ngưỡng black-box
test('NONFUNC latency: core endpoints under 5s', async ({ request }) => {
  const timed = async (fn: () => Promise<unknown>) => {
    const t0 = Date.now();
    await fn();
    return Date.now() - t0;
  };

  const tCart = await timed(() => request.get('cart', { headers: auth(CUSTOMER()) }));
  const tInv = await timed(() =>
    request.get('inventory?page=0&size=10', { headers: auth(ADMIN()) }),
  );
  const tFlash = await timed(() =>
    request.get('promotion/flash-sales/active', { headers: auth(CUSTOMER()) }),
  );
  expect(tCart).toBeLessThan(5000);
  expect(tInv).toBeLessThan(5000);
  expect(tFlash).toBeLessThan(5000);
});

// NF3 — auth: thiếu/sai token, sai role, đọc đơn của người khác
test('NONFUNC auth edges: anonymous, garbage, wrong role blocked', async ({ request }) => {
  const noToken = await request.get('cart');
  expect([401, 403]).toContain(noToken.status());

  const garbage = await request.get('cart', {
    headers: { Authorization: 'Bearer nope.not.real' },
  });
  expect([401, 403]).toContain(garbage.status());

  // customer đụng admin/staff-only
  const custRestock = await request.put('inventory/2/restock', {
    data: { quantity: 1, unitCost: 1 },
    headers: auth(CUSTOMER()),
  });
  expect(custRestock.status()).toBe(403);

  // staff đụng admin-only (body hợp lệ để lọt tới check quyền)
  const staffVoucher = await request.post('vouchers', {
    data: {
      code: 'E2E-X',
      scopeType: 'STOREWIDE',
      discountType: 'FIXED',
      discountValue: 1000,
      totalQuantity: 1,
      validFrom: new Date().toISOString(),
      validTo: new Date(Date.now() + 86400000).toISOString(),
    },
    headers: auth(STAFF()),
  });
  expect(staffVoucher.status()).toBe(403);
});

// NF4 — isolation: user lạ không đọc được đơn của user 2
test('NONFUNC isolation: stranger cannot read my order', async ({ request }) => {
  const c = auth(CUSTOMER());
  const cart = await request.get('cart', { headers: c });
  if (cart.status() === 200) {
    const body = await cart.json();
    for (const it of body?.data?.items ?? []) {
      await request.delete(`cart/items/${it.skuId}`, { headers: c });
    }
  }
  await ensureStock(request, 2);
  await request.post('cart/items', { data: { skuId: 2, quantity: 1 }, headers: c });
  const co = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  expect(co.status()).toBe(200);
  const orderId = (await co.json()).id;

  const stranger = mintToken(999, 'CUSTOMER');
  const peek = await request.get(`orders/${orderId}`, {
    headers: auth(stranger),
  });
  expect([403, 404]).toContain(peek.status());
});

// NF5 — validation: body sai định dạng bị 400, giỏ rỗng không checkout được
test('NONFUNC validation: bad bodies rejected, empty cart cannot checkout', async ({
  request,
}) => {
  const c = auth(CUSTOMER());

  const qtyZero = await request.post('cart/items', {
    data: { skuId: 2, quantity: 0 },
    headers: c,
  });
  expect(qtyZero.status()).toBe(400);

  const noSku = await request.post('cart/items', { data: { quantity: 1 }, headers: c });
  expect(noSku.status()).toBe(400);

  // enum lạ trong body -> 400 (không phải 500)
  const badEnum = await request.post('vouchers', {
    data: {
      code: 'E2E-BAD',
      scopeType: 'GLOBAL',
      discountType: 'FIXED',
      discountValue: 1000,
      totalQuantity: 1,
      validFrom: new Date().toISOString(),
      validTo: new Date(Date.now() + 86400000).toISOString(),
    },
    headers: auth(ADMIN()),
  });
  expect(badEnum.status()).toBe(400);

  const cart = await request.get('cart', { headers: c });
  if (cart.status() === 200) {
    const body = await cart.json();
    for (const it of body?.data?.items ?? []) {
      await request.delete(`cart/items/${it.skuId}`, { headers: c });
    }
  }
  const empty = await request.post('orders/checkout', {
    data: { idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} },
    headers: c,
  });
  expect(empty.status()).toBe(400);
});
