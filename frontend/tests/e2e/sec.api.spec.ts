import { test, expect } from '@playwright/test';
import { ADMIN, CUSTOMER, auth, mintToken } from './auth.helper';

const SQLI = [
  `' OR '1'='1`,
  `1; DROP TABLE users;--`,
  `' UNION SELECT id,email,password_hash FROM users--`,
  `1' AND SLEEP(5)--`,
];

const XSS = `<script>alert(document.domain)</script><img src=x onerror=alert(1)>`;

// SEC1 — SQLi vào ô search: không 500, không lộ SQL trong body
test('SEC sqli in search returns clean error, no leakage', async ({ request }) => {
  for (const p of SQLI) {
    const r = await request.get(`skus?page=0&size=5&search=${encodeURIComponent(p)}`, {
      headers: auth(CUSTOMER()),
    });
    expect([200, 400]).toContain(r.status());
    const body = await r.text();
    expect(body.toLowerCase()).not.toContain('syntax error');
    expect(body.toLowerCase()).not.toContain('psqlexception');
    expect(body).not.toContain('password_hash');
  }
});

// SEC2 — SQLi vào voucher code validate
test('SEC sqli in voucher code is inert', async ({ request }) => {
  for (const p of SQLI.slice(0, 2)) {
    const r = await request.post('vouchers/validate', {
      data: { code: p, orderTotal: 1000000 },
      headers: auth(CUSTOMER()),
    });
    expect([200, 400]).toContain(r.status());
    expect((await r.text()).toLowerCase()).not.toContain('psqlexception');
  }
});

// SEC3 — XSS lưu trong review: API trả JSON (không execute), BE không 500
test('SEC xss in review comment stored inert, json content-type', async ({ request }) => {
  const r = await request.post('reviews', {
    data: { skuId: 2, subOrderId: 1, rating: 1, comment: XSS, images: [] },
    headers: auth(CUSTOMER()),
  });
  expect([200, 201, 400, 404, 422]).toContain(r.status());
  const ct = r.headers()['content-type'] ?? '';
  expect(ct).toContain('application/json');
});

// SEC4 — token cạo sửa chữ ký / sai secret bị từ chối
test('SEC tampered token rejected', async ({ request }) => {
  const good = mintToken(2, 'CUSTOMER');
  const bad = good.slice(0, -2) + (good.endsWith('AA') ? 'BB' : 'AA');
  const r = await request.get('cart', { headers: auth(bad) });
  expect([401, 403]).toContain(r.status());
});

// SEC5 — role lạ trong token không leo thang
test('SEC unknown role cannot access', async ({ request }) => {
  const weird = mintToken(2, 'SUPERADMIN' as never);
  const r = await request.get('cart', { headers: auth(weird) });
  expect([401, 403]).toContain(r.status());
});

// SEC6 — path traversal / id dị dạng ở path param
test('SEC malformed path params rejected, no stack leak', async ({ request }) => {
  const c = auth(CUSTOMER());
  for (const p of ['../admin', 'abc', '-1', '99999999999999999999']) {
    const r = await request.get(`skus/${p}`, { headers: c });
    expect([400, 403, 404]).toContain(r.status());
    expect((await r.text()).toLowerCase()).not.toContain('stacktrace');
  }
});

// SEC7 — body quá khổ / sai kiểu không crash (400 chứ không 500)
test('SEC oversized and mistyped bodies get 400, never 500', async ({ request }) => {
  const c = auth(CUSTOMER());
  const big = await request.post('reviews', {
    data: { skuId: 2, subOrderId: 1, rating: 1, comment: 'x'.repeat(20000), images: [] },
    headers: c,
  });
  expect([200, 201, 400, 404, 413, 422]).toContain(big.status());
  expect(big.status()).not.toBe(500);

  const mistyped = await request.post('cart/items', {
    data: { skuId: 'hai', quantity: 'nhieu' },
    headers: c,
  });
  expect([400, 404, 422]).toContain(mistyped.status());
  expect(mistyped.status()).not.toBe(500);
});

// SEC8 — admin route với token thường: 403 đồng đều, không rò rỉ
test('SEC admin routes uniformly forbidden for customer', async ({ request }) => {
  const c = auth(CUSTOMER());
  const routes = [
    'admin/reports/revenue?from=2024-01-01T00:00:00&to=2024-06-01T00:00:00',
    'vouchers?page=0&size=5',
  ];
  for (const p of routes) {
    const r = await request.get(p, { headers: c });
    expect(r.status()).toBe(403);
  }
  expect(auth(ADMIN())).toBeTruthy();
});
