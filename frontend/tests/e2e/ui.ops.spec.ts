import { test, expect } from '@playwright/test';

// Ops admin UI tour (Playwright, headed by default): login as mock admin,
// walk the staff pages, assert each renders with data. Mock APIs serve admin
// data, so no backend seed is needed.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

async function loginAsAdmin(page) {
  // Login on the returnUrl page so post-login navigation stays client-side:
  // a cold goto(/admin) 302s through SSR login (server has no session).
  await page.goto(`${UI}/auth/login?returnUrl=/admin`);
  await page.locator('#email').fill('admin@dawn.vn');
  await page.locator('#password').fill('admin123');
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
  await page.locator('li.ant-menu-item', { hasText: 'Trả hàng' }).first().click();
  await expect(page).toHaveURL(/\/admin\/returns$/, { timeout: 15000 });
  // First mock request is PENDING and not self-created, so Duyệt is enabled.
  await page.getByRole('link', { name: /xem chi tiết/i }).first().click();
  await expect(page.getByText('Chờ duyệt').first()).toBeVisible({ timeout: 20000 });
  await page.getByRole('button', { name: 'Duyệt' }).click();
  await expect(page.getByText('Đã duyệt').first()).toBeVisible({ timeout: 15000 });
  await expect(page.locator('.ant-message-notice')).toContainText(/đã duyệt yêu cầu/i);
});

test('ops reviews: hide a pending review', async ({ page }) => {
  await loginAsAdmin(page);
  await page.locator('li.ant-menu-item', { hasText: 'Đánh giá' }).first().click();
  await expect(page).toHaveURL(/\/admin\/reviews$/, { timeout: 15000 });
  await page.locator('nz-select').click();
  await page.locator('.ant-select-item-option', { hasText: 'Chờ duyệt' }).click();
  const rows = page.locator('tbody tr');
  await expect(rows).toHaveCount(3, { timeout: 15000 });
  await page.getByRole('button', { name: /^ẩn$/i }).first().click();
  await expect(page.locator('.ant-message-notice')).toContainText(/đã ẩn/i);
  await expect(rows).toHaveCount(2, { timeout: 15000 });
});

test('ops admin tour: dashboard, orders, catalog, staff pages', async ({ page }) => {
  await loginAsAdmin(page);
  await expect(page.getByRole('heading', { name: /Dashboard/ })).toBeVisible({ timeout: 20000 });

  // Sidebar clicks keep navigation client-side: a cold goto() would 302
  // through SSR login first (server has no session).
  const tour: [string, RegExp, RegExp, string][] = [
    ['Đơn hàng', /\/admin\/orders$/, /Quản lý đơn hàng/, 'ops-orders.png'],
    ['Sản phẩm', /\/admin\/products$/, /Sản phẩm/, 'ops-products.png'],
    ['Kho hàng', /\/admin\/inventory$/, /Quản lý tồn kho/, 'ops-inventory.png'],
    ['Khuyến mãi', /\/admin\/vouchers$/, /Quản lý mã giảm giá/, 'ops-vouchers.png'],
    ['Người dùng', /\/admin\/users$/, /Người dùng/, 'ops-users.png'],
    ['Đánh giá', /\/admin\/reviews$/, /Đánh giá/, 'ops-reviews.png'],
    ['Trả hàng', /\/admin\/returns$/, /Yêu cầu trả hàng/, 'ops-returns.png'],
    ['Danh mục', /\/admin\/categories$/, /Danh mục/, 'ops-categories.png'],
  ];

  for (const [label, url, heading, shot] of tour) {
    // Scope to real menu items: section headers share the same text (uppercased by CSS).
    await page.locator('li.ant-menu-item', { hasText: label }).first().click();
    await expect(page).toHaveURL(url, { timeout: 15000 });
    await expect(page.getByRole('heading', { name: heading }).first()).toBeVisible({ timeout: 20000 });
    // List pages show a table — proves data rendered.
    await expect(page.locator('nz-table, table').first()).toBeVisible({ timeout: 20000 });
    await page.screenshot({ path: `test-results/${shot}` });
  }
});
