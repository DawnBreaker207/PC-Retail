import { test, expect, Browser, Page, APIRequestContext } from '@playwright/test';
import { ADMIN, CUSTOMER, auth, ensureStock, guestPhone, guestOtpDance } from './auth.helper';

// Full real-backend saga, admin + member sides in one run (minted tokens
// start with eyJ so the mock interceptor passes everything through):
//   admin restock -> member purchase -> admin ships to DELIVERED
//   -> approve-case return -> reject/dispute/resolve-case return -> review.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';
const SKU = 1;

const adminSession = () => ({
  accessToken: ADMIN(),
  refreshToken: ADMIN(),
  user: { id: 1, fullName: 'E2E Admin', email: 'e2e-admin@t.vn', phone: '0900000001', systemRole: 'ADMIN' },
});
const memberSession = () => ({
  accessToken: CUSTOMER(),
  refreshToken: CUSTOMER(),
  user: { id: 2, fullName: 'E2E Member', email: 'e2e-member@t.vn', phone: '0900000002', systemRole: 'CUSTOMER' },
});

async function ctxWithSession(browser: Browser, session: object) {
  const ctx = await browser.newContext();
  await ctx.addInitScript((s) => localStorage.setItem('pc-retail-session', JSON.stringify(s)), session);
  return ctx;
}

async function stockOf(request: APIRequestContext, skuId: number): Promise<number> {
  const r = await request.get(`inventory?page=0&size=10&skuId=${skuId}`, { headers: auth(ADMIN()) });
  const item = (await r.json()).data?.content?.[0];
  return item?.availableQuantity ?? -1;
}

async function subOf(request: APIRequestContext, token: string, orderId: number) {
  const r = await request.get(`orders/${orderId}`, { headers: auth(token) });
  const body = await r.json();
  const o = body?.data ?? body;
  return o.subOrders[0];
}

async function shipToDelivered(admin: Page, orderId: number) {
  await admin.locator('li.ant-menu-item', { hasText: 'Đơn hàng' }).first().click();
  await expect(admin).toHaveURL(/\/admin\/orders$/, { timeout: 15000 });
  await admin.locator('input[aria-label*="Tìm mã đơn"]').fill(String(orderId));
  await admin.locator('input[aria-label*="Tìm mã đơn"]').press('Enter');
  await admin.waitForTimeout(1500);
  await admin.locator('a', { hasText: `#${orderId}` }).first().click();
  await admin.getByRole('button', { name: /xác nhận xử lý/i }).click();
  await expect(admin.getByText('Đang xử lý').first()).toBeVisible({ timeout: 15000 });
  await admin.locator('input[aria-label="Mã vận đơn"]').fill('TRACK-E2E');
  await admin.getByRole('button', { name: /đánh dấu đã giao shipper/i }).click();
  await expect(admin.getByText('Đang giao').first()).toBeVisible({ timeout: 15000 });
  await admin.getByRole('button', { name: /xác nhận đã giao/i }).click();
  await expect(admin.getByText('Đã giao').first()).toBeVisible({ timeout: 15000 });
}

async function payAsStaff(request: APIRequestContext, subOrderId: number) {
  const paid = await request.post(`staff/orders/sub-orders/${subOrderId}/transition`, {
    data: { status: 'PAID' },
    headers: auth(ADMIN()),
  });
  expect(paid.status()).toBe(200);
}

async function returnIdFor(request: APIRequestContext, subOrderId: number, status?: string): Promise<number> {
  const q = status ? `?status=${status}` : '';
  const r = await request.get(`staff/return-requests${q}`, { headers: auth(ADMIN()) });
  const body = await r.json();
  const d = body?.data ?? body;
  const arr = Array.isArray(d) ? d : d?.content ?? [];
  return arr.find((x) => x.subOrderId === subOrderId)?.id ?? 0;
}

async function purchaseAs(page: Page, request: APIRequestContext): Promise<number> {
  await page.goto(`${UI}/products`);
  await page.waitForTimeout(2000);
  await page
    .locator('app-product-card', { hasText: 'CMHH2VN893SH' })
    .first()
    .locator('a[href*="/products/"]')
    .first()
    .click();
  await page.waitForTimeout(2500);
  await page.locator('button', { hasText: 'Thêm vào giỏ' }).first().click();
  await page.waitForTimeout(1500);
  const badge = page.locator('header button[aria-label="Giỏ hàng"] .ant-badge-count');
  expect(await badge.count()).toBeGreaterThan(0);
  expect((await badge.first().textContent())?.trim()).not.toBe('');

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);
  await page.goto(`${UI}/checkout`);
  await page.waitForTimeout(1500);
  const phone = guestPhone();
  await page.locator('#fullName').fill('Khach Fullflow');
  await page.locator('#phone').fill(phone);
  await page.locator('#city').click();
  await page.waitForTimeout(500);
  await page.locator('.ant-select-item-option').first().click();
  await page.locator('#district').fill('Hai Ba Trung');
  await page.locator('#street').fill('123 Duong Flow');

  if ((await page.getByRole('button', { name: /gửi mã otp/i }).count()) > 0) {
    await page.getByRole('button', { name: /gửi mã otp/i }).click();
    await page.waitForTimeout(800);
    const q = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
    const m = q.match(/(\d+)\s*\+\s*(\d+)/);
    expect(m).toBeTruthy();
    await page.locator('#challengeAnswer').fill(String(Number(m![1]) + Number(m![2])));
    await page.getByRole('button', { name: /gửi mã otp/i }).click();
    await page.waitForTimeout(800);
    await guestOtpDance(request, phone);
    await page.reload();
    await page.waitForTimeout(2000);
    await page.locator('#fullName').fill('Khach Fullflow');
    await page.locator('#phone').fill(phone);
    await page.locator('#city').click();
    await page.waitForTimeout(500);
    await page.locator('.ant-select-item-option').first().click();
    await page.locator('#district').fill('Hai Ba Trung');
    await page.locator('#street').fill('123 Duong Flow');
  }

  await page.getByRole('button', { name: /tiếp tục/i }).click();
  await page.waitForTimeout(2000);
  await page.getByRole('button', { name: /đặt hàng/i }).click();
  await page.waitForTimeout(4000);
  expect(page.url()).toContain('result');
  const id = Number(new URL(page.url()).searchParams.get('orderId'));
  expect(id).toBeGreaterThan(0);
  return id;
}

test('full saga: admin stocks, member buys, both sides do after-sales', async ({ browser, request }) => {
  test.setTimeout(600_000);
  await ensureStock(request, SKU, 30, 80);
  await request.delete('cart', { headers: auth(CUSTOMER()) });

  const adminCtx = await ctxWithSession(browser, adminSession());
  const memberCtx = await ctxWithSession(browser, memberSession());
  const admin = await adminCtx.newPage();
  const user = await memberCtx.newPage();

  // A. Admin restocks through the UI; stock must grow by exactly the top-up.
  const row = admin.locator('tbody tr').first();
  await admin.goto(`${UI}/auth/login?returnUrl=/admin/inventory`);
  await expect(admin).toHaveURL(/\/admin\/inventory$/, { timeout: 20000 });
  const skuId = Number(((await row.locator('td').first().textContent()) ?? '').replace(/\D/g, ''));
  expect(skuId).toBeGreaterThan(0);
  const before = await stockOf(request, skuId);
  await row.locator('button', { hasText: 'Nhập thêm' }).click();
  await admin.locator('#restock-quantity input').fill('7');
  await admin.locator('#restock-quantity input').press('Enter');
  await admin.locator('#restock-note').fill('e2e fullflow top-up');
  await admin.getByRole('button', { name: /xác nhận nhập/i }).click();
  await expect.poll(async () => stockOf(request, skuId), { timeout: 20000 }).toBe(before + 7);

  // B. Member buys (real backend order).
  const orderA = await purchaseAs(user, request);

  // C. Staff marks it PAID (API, like UAT), admin ships to DELIVERED (UI).
  const subA = await subOf(request, CUSTOMER(), orderA);
  await payAsStaff(request, subA.id);
  await shipToDelivered(admin, orderA);

  // D1. Member requests a return on the delivered order (direct URL: the
  // result page only links order lookup for unpaid orders).
  await user.goto(`${UI}/orders/${orderA}`);
  await expect(user).toHaveURL(new RegExp(`/orders/${orderA}$`), { timeout: 20000 });
  await expect(user.getByText('Đã giao').first()).toBeVisible({ timeout: 20000 });
  user.on('dialog', (d) => d.accept('Hàng lỗi, muốn đổi mới'));
  // Synthetic click: with watch-mode slowMo the layout can shift between
  // mousedown/mouseup and silently void a real click on this button.
  await user.getByRole('button', { name: /request return/i }).dispatchEvent('click');
  let retA = 0;
  await expect.poll(async () => (retA = await returnIdFor(request, subA.id, 'REQUESTED')), { timeout: 20000 }).toBeGreaterThan(0);

  // E1. Admin approves through the UI.
  await admin.goto(`${UI}/admin/returns/${retA}`);
  await expect(admin.getByText('Chờ duyệt').first()).toBeVisible({ timeout: 20000 });
  await admin.getByRole('button', { name: 'Duyệt' }).click();
  const tApprove = Date.now();
  await expect(admin.getByText('Đã duyệt').first()).toBeVisible({ timeout: 180000 });
  // eslint-disable-next-line no-console
  console.log('APPROVE-VISIBLE-MS:', Date.now() - tApprove);

  // F1. Member sees the approval.
  await user.reload();
  await expect(user.getByText('Đã duyệt').first()).toBeVisible({ timeout: 25000 });

  // D2. Second order for the reject -> dispute -> resolve case.
  const orderB = await purchaseAs(user, request);
  const subB = await subOf(request, CUSTOMER(), orderB);
  await payAsStaff(request, subB.id);
  await shipToDelivered(admin, orderB);
  await user.goto(`${UI}/orders/${orderB}`);
  await expect(user.getByText('Đã giao').first()).toBeVisible({ timeout: 25000 });
  await user.getByRole('button', { name: /request return/i }).click();
  let retB = 0;
  await expect.poll(async () => (retB = await returnIdFor(request, subB.id, 'REQUESTED')), { timeout: 20000 }).toBeGreaterThan(0);

  // E2. Admin rejects with a reason through the UI.
  await admin.goto(`${UI}/admin/returns/${retB}`);
  await expect(admin.getByText('Chờ duyệt').first()).toBeVisible({ timeout: 20000 });
  await admin.getByRole('button', { name: /từ chối/i }).click();
  await admin.locator('.ant-modal-body textarea').fill('Không đủ điều kiện bảo hành');
  await admin.getByRole('button', { name: /từ chối yêu cầu/i }).click();
  await expect(admin.getByText('Đã từ chối').first()).toBeVisible({ timeout: 60000 });

  // F2. Member disputes through the UI, admin resolves through the UI.
  await user.goto(`${UI}/orders/${orderB}`);
  await expect(user.getByText('Đã từ chối').first()).toBeVisible({ timeout: 25000 });
  await user.getByRole('button', { name: /khiếu nại/i }).click();
  await expect(user.getByText('Đang khiếu nại').first()).toBeVisible({ timeout: 25000 });
  await admin.goto(`${UI}/admin/returns/${retB}`);
  await expect(admin.getByText('Đang khiếu nại').first()).toBeVisible({ timeout: 20000 });
  await admin.getByRole('button', { name: /chấp nhận khiếu nại/i }).click();
  await expect(admin.getByText('Đã duyệt').first()).toBeVisible({ timeout: 60000 });
  await user.goto(`${UI}/orders/${orderB}`);
  await expect(user.getByText('Đã duyệt').first()).toBeVisible({ timeout: 25000 });

  // G. Member reviews the delivered item of order A.
  await user.goto(`${UI}/orders/${orderA}`);
  await expect(user.getByText('Đã giao').first()).toBeVisible({ timeout: 25000 });
  await user.getByRole('button', { name: /viết đánh giá/i }).first().click();
  await user.locator('.ant-rate-star').nth(4).click();
  await user.locator('textarea').fill('Hàng chính hãng, đóng gói cẩn thận, e2e.');
  await user.getByRole('button', { name: /gửi đánh giá/i }).click();
  await expect(user.locator('.ant-message-notice')).toBeVisible({ timeout: 60000 });

  await adminCtx.close();
  await memberCtx.close();
});
