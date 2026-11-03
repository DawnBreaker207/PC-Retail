import { test, expect } from '@playwright/test';
import { ensureStock } from './auth.helper';

// Các luồng UI bổ sung (chạy headless trong suite; xem headed: --headed).
// Quy ước: chỉ test luồng đi API THẬT; luồng mock-driven (member checkout,
// tra cứu, builder) đã loại vì test mock không có giá trị.
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

// UIF1 — login: sai định dạng bị chặn, đúng thì vào trang chủ + có session
test('UI login: invalid blocked, mock admin lands home with session', async ({ page }) => {
  await page.goto(`${UI}/auth/login`);
  await page.waitForTimeout(1200);

  const submit = page.locator('form button[type="submit"]');
  await page.locator('#email').fill('khong-phai-email');
  await page.locator('#password').fill('123456');
  // Form sai thì nút submit disable — không cho bấm (UX đúng)
  await expect(submit).toBeDisabled({ timeout: 5000 });
  expect(page.url()).toContain('/auth/login');

  await page.locator('#email').fill('admin@pc-retail.com');
  await page.locator('#password').fill('AdminPassword123!');
  await expect(submit).toBeEnabled({ timeout: 5000 });
  await submit.click();
  await page.waitForTimeout(2000);
  expect(page.url()).not.toContain('/auth/login');
  const session = await page.evaluate(() => localStorage.getItem('pc-retail-session'));
  expect(session).toBeTruthy();
  expect(session).toContain('ADMIN');
});

// UIF2 — OTP sai: toast lỗi + nút Tiếp tục vẫn bị chặn (không sang bước 2)
test('UI OTP wrong code: toast error, cannot proceed', async ({ page, request }) => {
  await ensureStock(request, 1, 15);
  await page.goto(`${UI}/products/1`);
  await page.waitForTimeout(2000);
  await page.getByRole('button', { name: /thêm vào giỏ/i }).first().click();
  await page.waitForTimeout(1500);

  await page.goto(`${UI}/checkout`);
  await page.waitForTimeout(1500);
  const phone = `09${String(10000000 + Math.floor(Math.random() * 89999999))}`;
  await page.locator('#phone').fill(phone);
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(800);
  const q = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
  const m = q.match(/(\d+)\s*\+\s*(\d+)/);
  if (m) {
    await page.locator('#challengeAnswer').fill(String(Number(m[1]) + Number(m[2])));
    await page.getByRole('button', { name: /gửi mã otp/i }).click();
    await page.waitForTimeout(800);
  }

  const otpInput = page.locator('input[formControlName="otpCode"]');
  await expect(otpInput).toBeVisible({ timeout: 8000 });
  await otpInput.fill('000000');
  await page.getByRole('button', { name: /xác minh/i }).click();
  await page.waitForTimeout(800);

  // Chưa verify -> nút Tiếp tục xác nhận đơn hàng bị disabled
  const continueBtn = page.getByRole('button', { name: /tiếp tục xác nhận đơn hàng/i });
  await expect(continueBtn).toBeDisabled();
  expect(page.url()).toContain('/checkout');
});

// UIF3 — giỏ guest (API thật): tăng số lượng rồi xóa
test('UI guest cart: qty up then remove', async ({ page, request }) => {
  await ensureStock(request, 1, 15);
  await page.goto(`${UI}/products/1`);
  await page.waitForTimeout(2000);
  await page.getByRole('button', { name: /thêm vào giỏ/i }).first().click();
  await page.waitForTimeout(1500);

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(2000);
  const incBtn = page.locator('button[aria-label="Tăng số lượng"]').first();
  await expect(incBtn).toBeVisible({ timeout: 10000 });
  await incBtn.click();
  await page.waitForTimeout(1000);

  const remove = page.locator('button[aria-label="Xóa sản phẩm"]').first();
  await remove.click();
  await expect(page.locator('text=Giỏ hàng của bạn đang trống')).toBeVisible({ timeout: 10000 });
});
