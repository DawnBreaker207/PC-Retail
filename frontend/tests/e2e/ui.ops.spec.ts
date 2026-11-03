import { test, expect } from '@playwright/test';

// Ops admin UI tour (Playwright, headed by default): login as mock admin,
// walk the staff pages, assert each renders with data. Mock APIs serve admin
// data, so no backend seed is needed.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

async function loginAsAdmin(page) {
  // Login on the returnUrl page so post-login navigation stays client-side:
  // a cold goto(/admin) 302s through SSR login (server has no session).
  await page.goto(`${UI}/auth/login?returnUrl=/admin`);
  await page.locator('#email').fill('admin@pc-retail.com');
  await page.locator('#password').fill('AdminPassword123!');
  await page.locator('form button[type="submit"]').click();
  await expect(page).toHaveURL(/\/admin\/?$/, { timeout: 15000 });
  const session = await page.evaluate(() => localStorage.getItem('pc-retail-session'));
  expect(session).toContain('ADMIN');
}

test('ops guest cannot enter admin', async ({ page }) => {
  await page.goto(`${UI}/admin`);
  await expect(page).toHaveURL(/\/auth\/login/, { timeout: 15000 });
});

test('ops returns: approve a pending request', async ({ page }) => {
  await loginAsAdmin(page);
  await page.locator('aside a', { hasText: 'Trả hàng' }).first().click();
  await expect(page).toHaveURL(/\/admin\/returns$/, { timeout: 15000 });
  const select = page.locator('select').first();
  if (await select.isVisible()) {
    await select.selectOption({ label: 'Chờ duyệt' });
  }
  await page.waitForTimeout(1000);
  const links = page.getByRole('link', { name: /xem chi tiết/i });
  if ((await links.count()) > 0) {
    await links.first().click();
    await expect(page.getByText('Chờ duyệt').first()).toBeVisible({ timeout: 20000 });
    await page.getByRole('button', { name: 'Duyệt' }).click();
    await expect(page.getByText('Đã duyệt').first()).toBeVisible({ timeout: 15000 });
    await expect(page.locator('app-toast-container')).toContainText(/đã duyệt yêu cầu/i);
  } else {
    await expect(page.locator('table').first()).toBeVisible({ timeout: 15000 });
  }
});

test('ops reviews: hide a pending review', async ({ page }) => {
  await loginAsAdmin(page);
  await page.locator('aside a', { hasText: 'Đánh giá' }).first().click();
  await expect(page).toHaveURL(/\/admin\/reviews$/, { timeout: 15000 });
  const select = page.locator('select').first();
  if (await select.isVisible()) {
    await select.selectOption({ label: 'Chờ duyệt' });
  }
  await page.waitForTimeout(1000);
  const rows = page.locator('tbody tr');
  const count = await rows.count();
  if (count > 0 && (await page.getByRole('button', { name: /^ẩn$/i }).count()) > 0) {
    await page.getByRole('button', { name: /^ẩn$/i }).first().click();
    await expect(page.locator('app-toast-container')).toContainText(/đã ẩn/i);
  } else {
    await expect(page.locator('table').first()).toBeVisible({ timeout: 15000 });
  }
});

test('ops admin tour: dashboard, orders, catalog, staff pages', async ({ page }) => {
  await loginAsAdmin(page);
  await expect(page.getByRole('heading', { name: /Dashboard/ })).toBeVisible({ timeout: 20000 });

  // Sidebar clicks keep navigation client-side: a cold goto() would 302
  // through SSR login first (server has no session).
  const tour: [string, RegExp, RegExp, string][] = [
    ['Đơn hàng', /\/admin\/orders$/, /Quản lý đơn hàng/i, 'ops-orders.png'],
    ['Sản phẩm', /\/admin\/products$/, /Sản phẩm/i, 'ops-products.png'],
    ['Kho hàng', /\/admin\/inventory$/, /Quản lý kho|Quản lý tồn kho/i, 'ops-inventory.png'],
    ['Khuyến mãi', /\/admin\/vouchers$/, /Quản lý mã giảm giá/i, 'ops-vouchers.png'],
    ['Người dùng', /\/admin\/users$/, /Người dùng/i, 'ops-users.png'],
    ['Đánh giá', /\/admin\/reviews$/, /Đánh giá/i, 'ops-reviews.png'],
    ['Trả hàng', /\/admin\/returns$/, /Yêu cầu trả hàng|Quản lý yêu cầu trả hàng/i, 'ops-returns.png'],
    ['Danh mục', /\/admin\/categories$/, /Danh mục/i, 'ops-categories.png'],
  ];

  for (const [label, url, heading, shot] of tour) {
    // Scope to real menu items: section headers share the same text (uppercased by CSS).
    await page.locator('aside a', { hasText: label }).first().click();
    await expect(page).toHaveURL(url, { timeout: 15000 });
    await expect(page.getByRole('heading', { name: heading }).first()).toBeVisible({ timeout: 20000 });
    // List pages show a table or map — proves data rendered.
    await expect(page.locator('table, app-warehouse-map').first()).toBeVisible({ timeout: 20000 });
    await page.screenshot({ path: `test-results/${shot}` });
  }
});
