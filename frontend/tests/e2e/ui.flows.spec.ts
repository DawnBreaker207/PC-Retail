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

  await page.locator('#email').fill('admin@dawn.vn');
  await page.locator('#password').fill('admin123');
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

  await page.goto(`${UI}/checkout`);
  await page.waitForTimeout(1500);
  const phone = `09${String(10000000 + Math.floor(Math.random() * 89999999))}`;
  await page.locator('#phone').fill(phone);
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(800);
  const q = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
  const m = q.match(/(\d+)\s*\+\s*(\d+)/);
  await page.locator('#challengeAnswer').fill(String(Number(m![1]) + Number(m![2])));
  await page.getByRole('button', { name: /gửi mã otp/i }).click();
  await page.waitForTimeout(800);

  const boxes = page.locator('nz-input-otp input');
  await expect(boxes).toHaveCount(6, { timeout: 8000 });
  await boxes.first().click();
  await boxes.first().pressSequentially('000000', { delay: 40 });
  await page.getByRole('button', { name: /xác minh/i }).click();
  await page.waitForTimeout(800);
  await expect(page.locator('.ant-message-notice')).toHaveCount(1, { timeout: 5000 });

  // Chưa verify -> bấm Tiếp tục không sang bước xác nhận
  await page.locator('#city').click();
  await page.waitForTimeout(500);
  await page.locator('.ant-select-item-option').first().click();
  await page.locator('#district').fill('Hai Ba Trung');
  await page.locator('#street').fill('123 Duong Demo');
  await page.locator('#fullName').fill('Khach Demo');
  await page.getByRole('button', { name: /tiếp tục/i }).click();
  await page.waitForTimeout(1200);
  expect(page.url()).toContain('/checkout');
  await expect(page.locator('#phone')).toBeVisible();
});

// UIF3 — giỏ guest (API thật): tăng số lượng rồi xóa
test('UI guest cart: qty up then remove', async ({ page, request }) => {
  await ensureStock(request, 1, 15);
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

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(2000);
  const qty = page.locator('nz-input-number input');
  await expect(qty.first()).toBeVisible({ timeout: 10000 });
  await qty.first().fill('2');
  await page.waitForTimeout(1200);

  const remove = page.getByRole('button', { name: /xóa/i }).first();
  await remove.click();
  const confirm = page.locator('.ant-popover').getByRole('button', { name: 'OK' });
  await expect(confirm.first()).toBeVisible({ timeout: 8000 });
  await confirm.first().click();
  await expect(page.locator('text=Giỏ hàng trống')).toBeVisible({ timeout: 10000 });
});
