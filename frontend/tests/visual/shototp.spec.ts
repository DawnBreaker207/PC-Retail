import { test } from '@playwright/test';

test('shot otp block', async ({ page }) => {
  await page.goto('http://localhost:4200/products');
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
  await page.goto('http://localhost:4200/checkout');
  await page.waitForTimeout(2000);
  await page.locator('#phone').fill('0912345678');
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(1200);
  await page.screenshot({ path: 'test-results/ui-otp.png', fullPage: false });
});
