import { test, expect } from '@playwright/test';
import { CUSTOMER, auth } from './auth.helper';

const UI = process.env.E2E_UI_URL || 'http://localhost:4200';

function memberSession(): string {
  const token = CUSTOMER();
  return JSON.stringify({
    accessToken: token,
    refreshToken: 'mock-refresh-token',
    user: {
      id: 30077,
      email: 'customer@pc-retail.com',
      fullName: 'Khách Hàng Mẫu',
      systemRole: 'CUSTOMER',
      role: 'CUSTOMER',
    },
  });
}

test.describe('UI Extended Features Verification', () => {
  test('UI Promotions: view campaigns and vouchers on /khuyen-mai', async ({ page }) => {
    await page.goto(`${UI}/khuyen-mai`);
    await page.waitForTimeout(1000);
    await expect(page.locator('body')).not.toBeEmpty();
    // Trang khuyến mãi hiển thị banner hoặc danh sách mã ưu đãi
    const heading = page.locator('h1, h2, .hero-title').first();
    await expect(heading).toBeVisible({ timeout: 15000 });
  });

  test('UI PC Builder: verify slots and summary render on /pc-builder/custom', async ({ page }) => {
    await page.goto(`${UI}/pc-builder/custom`);
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).not.toBeEmpty();
    // Kiểm tra giao diện cấu hình PC (tiêu đề hoặc danh sách linh kiện)
    const title = page.getByText(/build pc|cấu hình/i).first();
    await expect(title).toBeVisible({ timeout: 15000 });
  });

  test('UI Order Lookup: tra cuu don hang with orderId and phone', async ({ page }) => {
    await page.goto(`${UI}/tra-cuu-don-hang`);
    await page.waitForTimeout(1000);
    await expect(page.locator('body')).not.toBeEmpty();
    const orderInput = page.locator('input[formControlName="orderId"]');
    const phoneInput = page.locator('input[formControlName="phone"]');
    await expect(orderInput).toBeVisible({ timeout: 10000 });
    await expect(phoneInput).toBeVisible({ timeout: 10000 });

    await orderInput.fill('1');
    await phoneInput.fill('0912345678');
    await page.getByRole('button', { name: /tra cứu/i }).click();
    await page.waitForTimeout(1000);
    // Kết quả hoặc thông báo tra cứu hiển thị (không crash)
    await expect(page.locator('body')).not.toBeEmpty();
  });

  test('UI Account: view profile, address book, vouchers with member session', async ({ browser }) => {
    const ctx = await browser.newContext();
    await ctx.addInitScript((session) => {
      localStorage.setItem('pc-retail-session', session);
    }, memberSession());

    const page = await ctx.newPage();
    await page.goto(`${UI}/account`);
    await page.waitForTimeout(1500);

    // Profile name / email should render
    await expect(page.getByText(/Tài khoản của tôi/i).first()).toBeVisible({ timeout: 15000 });
    await expect(page.locator('input[type="email"], input[formControlName="email"]')).toHaveValue('customer@pc-retail.com', { timeout: 15000 });

    await ctx.close();
  });

  test('UI Wishlist: view wishlist page /yeu-thich with member session', async ({ browser }) => {
    const ctx = await browser.newContext();
    await ctx.addInitScript((session) => {
      localStorage.setItem('pc-retail-session', session);
    }, memberSession());

    const page = await ctx.newPage();
    await page.goto(`${UI}/yeu-thich`);
    await page.waitForTimeout(1500);

    // Trang yêu thích render đúng tiêu đề
    await expect(page.locator('h1')).toContainText('Sản phẩm yêu thích', { timeout: 15000 });

    await ctx.close();
  });
});
