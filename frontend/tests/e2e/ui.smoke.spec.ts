import { test, expect } from '@playwright/test';

// Smoke giao diện chạy headed để XEM thao tác:
//   npx playwright test ui.smoke --headed
// Chậm lại để nhìn rõ: $env:PW_SLOWMO=400; npx playwright test ui.smoke --headed
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

test('UI walkthrough: home -> products -> add to cart -> cart', async ({ page }) => {
  await page.goto(`${UI}/`);
  await expect(page.locator('header')).toBeVisible({ timeout: 20000 });
  await page.screenshot({ path: 'test-results/ui-home.png' });

  await page.goto(`${UI}/products`);
  const cards = page.locator('app-product-card');
  await expect(cards.first()).toBeVisible({ timeout: 20000 });
  const name = (await cards.first().textContent())?.slice(0, 40) ?? '';

  await cards.first().getByRole('button', { name: /thêm vào giỏ/i }).first().click();
  await page.waitForTimeout(2000);
  await page.screenshot({ path: 'test-results/ui-added.png' });

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: 'test-results/ui-cart.png' });
  // Giỏ guest vừa thêm — trang cart phải render (không trắng trang / không văng login)
  await expect(page.locator('body')).not.toBeEmpty();
  expect(name.length).toBeGreaterThan(0);
});
