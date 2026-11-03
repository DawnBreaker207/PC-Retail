import { test, expect } from '@playwright/test';

test.describe('Verify All Detail Views (Manual Test Parity)', () => {

  test('1. Product Detail Page renders completely', async ({ page }) => {
    // Navigate to products and find the first product
    await page.goto('http://localhost:4200/products', { waitUntil: 'networkidle' });
    await page.waitForTimeout(500);
    const card = page.locator('app-product-card a').first();
    const link = await card.getAttribute('href');
    await page.goto(`http://localhost:4200${link}`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Verify Title or Product Name exists
    const heading = page.locator('h1');
    await expect(heading).toBeVisible({ timeout: 5000 });
    const text = await heading.textContent();
    console.log('Product H1:', text);

    // Verify Price and Add-to-cart button
    const price = page.locator('text=₫').first();
    await expect(price).toBeVisible();

    const addBtn = page.getByRole('button', { name: /Thêm vào giỏ|Mua ngay/i }).first();
    await expect(addBtn).toBeVisible();

    await page.screenshot({ path: 'test-results/verify-product-detail.png', fullPage: true });
  });

  test('2. Customer Order Detail Page renders properly', async ({ page }) => {
    // Login as customer
    await page.goto('http://localhost:4200/auth/login');
    await page.locator('input[type="email"], input[name="email"]').fill('customer@pc-retail.com');
    await page.locator('input[type="password"], input[name="password"]').fill('CustomerPassword123!');
    await page.getByRole('button', { name: /Đăng nhập/i }).click();
    await page.waitForURL(url => !url.pathname.includes('/auth/login'), { timeout: 10000 });

    // Navigate to order detail 225
    await page.goto('http://localhost:4200/orders/225', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Check that empty state or order details card is rendered (NOT blank screen)
    const content = page.locator('app-order-detail');
    await expect(content).toBeVisible();

    // Verify order header or fallback card
    const orderTitle = page.getByText(/Chi tiết đơn hàng|Đơn hàng #|Không tìm thấy đơn hàng/i).first();
    await expect(orderTitle).toBeVisible();

    await page.screenshot({ path: 'test-results/verify-customer-order-detail.png', fullPage: true });
  });

  test('3. Admin Order Detail Page renders properly', async ({ page }) => {
    // Clear cookies/localStorage and login as admin
    await page.context().clearCookies();
    await page.goto('http://localhost:4200/auth/login');
    await page.evaluate(() => localStorage.clear());
    await page.reload();

    await page.locator('input[type="email"], input[name="email"]').fill('admin@pc-retail.com');
    await page.locator('input[type="password"], input[name="password"]').fill('AdminPassword123!');
    await page.getByRole('button', { name: /Đăng nhập/i }).click();
    await page.waitForURL(url => !url.pathname.includes('/auth/login'), { timeout: 10000 });

    // Navigate to admin order detail 225
    await page.goto('http://localhost:4200/admin/orders/225', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    const adminContent = page.getByText(/Chi tiết đơn hàng|Đơn hàng #|Quay lại danh sách/i).first();
    await expect(adminContent).toBeVisible();

    await page.screenshot({ path: 'test-results/verify-admin-order-detail.png', fullPage: true });
  });
});
