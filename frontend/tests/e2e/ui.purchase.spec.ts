import { test, expect } from '@playwright/test';
import { ensureStock } from '../e2e/auth.helper';

// Full guest purchase trong browser thật (đã validate tay): browse -> detail ->
// thêm giỏ -> cart -> checkout form -> review -> đặt hàng -> result.
// Chạy trong suite vì đã ổn định; tốn ~20s.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

test('UI full guest purchase: browse, add, checkout, pay, result', async ({
  page,
  request,
}) => {
  await ensureStock(request, 1, 15);
  page.on('dialog', (d) => d.accept());
  await page.goto(`${UI}/`);
  await page.waitForTimeout(1500);

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
  await page.locator('#fullName').fill('Khach Demo');
  await page.locator('#phone').fill('0912345678');
  await page.locator('#city').click();
  await page.waitForTimeout(500);
  await page.locator('.ant-select-item-option').first().click();
  await page.locator('#district').fill('Hai Ba Trung');
  await page.locator('#street').fill('123 Duong Demo');
  await page.getByRole('button', { name: /tiếp tục/i }).click();
  await page.waitForTimeout(2000);

  await page.getByRole('button', { name: /đặt hàng/i }).click();
  await page.waitForTimeout(4000);
  console.log('AFTER-ORDER-URL=' + page.url());
  expect(page.url()).toContain('result');
});
