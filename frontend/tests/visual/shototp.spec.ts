import { test, expect } from '@playwright/test';

test('shot otp block', async ({ page, request }) => {
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
  const phone = `09${String(10000000 + Math.floor(Math.random() * 89999999))}`;
  await page.locator('#phone').fill(phone);
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(1200);
  await page.screenshot({ path: 'test-results/ui-otp.png', fullPage: false });

  // Điền captcha + gửi, lấy debug code, điền ô OTP chuẩn
  const q = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
  const m = q.match(/(\d+)\s*\+\s*(\d+)/);
  await page.locator('#challengeAnswer').fill(String(Number(m![1]) + Number(m![2])));
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(1000);
  const code = await request
    .get(`auth/otp/debug/${phone}`)
    .then((r) => r.json())
    .then((b) => b.data.code as string);
  const boxes = page.locator('nz-input-otp input');
  await expect(boxes).toHaveCount(6, { timeout: 8000 });
  await boxes.first().click();
  await boxes.first().pressSequentially(code, { delay: 60 });
  await page.waitForTimeout(600);
  await page.screenshot({ path: 'test-results/ui-otp-boxes.png', fullPage: false });
});
