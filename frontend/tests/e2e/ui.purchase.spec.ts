import { test, expect } from '@playwright/test';
import { ensureStock, guestPhone } from '../e2e/auth.helper';

// Full guest purchase trong browser thật (đã validate tay): browse -> detail ->
// thêm giỏ -> cart -> checkout form (+OTP SĐT docs 13.5) -> review -> đặt hàng -> result.
// Chạy trong suite vì đã ổn định; tốn ~25s.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

test('UI full guest purchase: browse, add, checkout, pay, result', async ({
  page,
  request,
}) => {
  await ensureStock(request, 1, 15);
  const phone = guestPhone();
  await page.goto(`${UI}/`);
  await page.waitForTimeout(1500);

  await page.goto(`${UI}/products/1`);
  await page.waitForTimeout(2000);

  await page.getByRole('button', { name: /thêm vào giỏ/i }).first().click();
  await page.waitForTimeout(1500);
  const badge = page.locator('header a[aria-label*="Giỏ hàng"] span');
  expect(await badge.count()).toBeGreaterThan(0);
  expect((await badge.first().textContent())?.trim()).not.toBe('');

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);

  await page.goto(`${UI}/checkout`);
  await page.waitForTimeout(1500);
  await page.locator('#fullName').fill('Khach Demo');
  await page.locator('#phone').fill(phone);
  await page.locator('#city').selectOption({ index: 1 });
  await page.locator('#district').fill('Hai Ba Trung');
  await page.locator('#street').fill('123 Duong Demo');

  // OTP docs 13.5: bấm Gửi mã (lấy đề captcha) -> giải -> bấm Gửi mã (gửi OTP)
  // -> lấy debugCode -> điền -> Xác minh
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(800);
  const q = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
  const m = q.match(/(\d+)\s*\+\s*(\d+)/);
  expect(m).toBeTruthy();
  await page.locator('#challengeAnswer').fill(String(Number(m![1]) + Number(m![2])));
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(800);
  const dbg = await request.get(`auth/otp/debug/${phone}`);
  expect(dbg.status()).toBe(200);
  const code = (await dbg.json()).data.code as string;
  const otpInput = page.locator('input[formControlName="otpCode"]');
  await expect(otpInput).toBeVisible({ timeout: 8000 });
  await otpInput.fill(code);
  await page.getByRole('button', { name: /xác minh/i }).click();
  await page.waitForTimeout(800);
  await expect(page.locator('text=Đã xác minh')).toBeVisible({ timeout: 8000 });

  await page.getByRole('button', { name: /tiếp tục xác nhận đơn hàng/i }).click();
  await page.waitForTimeout(2000);

  await page.getByRole('button', { name: /xác nhận đặt hàng & thanh toán/i }).click();
  await page.waitForTimeout(4000);
  console.log('AFTER-ORDER-URL=' + page.url());
  expect(page.url()).toContain('result');
});
