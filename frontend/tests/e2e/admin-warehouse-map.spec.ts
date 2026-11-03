import { test, expect } from '@playwright/test';
import { ADMIN } from './auth.helper';

test.describe('Visual Warehouse Map in Admin Inventory', () => {

  test('1. Admin can access inventory and switch to Visual Warehouse Map', async ({ page }) => {
    // Authenticate as admin session
    const token = ADMIN();
    await page.addInitScript((jwt: string) => {
      const session = {
        accessToken: jwt,
        refreshToken: jwt,
        user: {
          id: 1,
          fullName: 'Admin Quản Trị Kho',
          email: 'admin@pc-retail.com',
          systemRole: 'ADMIN',
        },
      };
      localStorage.setItem('pc-retail-session', JSON.stringify(session));
    }, token);

    // Navigate to inventory
    await page.goto('http://localhost:4200/admin/inventory', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Verify Title
    await expect(page.getByText('Quản lý kho & Tồn kho')).toBeVisible();

    // Switch to Visual Map view
    const mapBtn = page.getByRole('button', { name: /Bản đồ kho trực quan/i });
    await expect(mapBtn).toBeVisible();
    await mapBtn.click();
    await page.waitForTimeout(500);

    // Verify Top KPI Cards
    await expect(page.getByText('Tổng vị trí ô ngăn')).toBeVisible();
    await expect(page.getByText('Tỷ lệ lấp đầy kho')).toBeVisible();
    await expect(page.getByText('Ô còn trống')).toBeVisible();
    await expect(page.getByText('Ô đã đầy (≥90%)')).toBeVisible();

    // Switch to Overview view to inspect physical entrance and all zones
    const overviewBtn = page.locator('button:has-text("Toàn cảnh kho")');
    if (await overviewBtn.isVisible()) {
      await overviewBtn.click();
      await page.waitForTimeout(500);
      await expect(page.getByText(/CỔNG CHÍNH \/ LỐI VÀO/i)).toBeVisible();
    }

    // Verify All Zones are visible in Overview Map
    await expect(page.getByText(/Khu A — Card màn hình/i)).toBeVisible();
    await expect(page.getByText(/Khu B — Bo mạch chủ/i)).toBeVisible();
    await expect(page.getByText(/Khu C — Bộ vi xử lý/i)).toBeVisible();
    await expect(page.getByText(/Khu D — Bộ nhớ RAM/i)).toBeVisible();
    await expect(page.getByText(/Khu E — Nguồn máy tính/i)).toBeVisible();

    // Screenshot 1: Overview Floor Map
    await page.screenshot({ path: 'test-results/admin-warehouse-map-overview.png', fullPage: true });

    // 2. Test Search & Highlight
    const searchInput = page.locator('input[placeholder*="SKU"]').first();
    await searchInput.fill('RTX 4090');
    await page.waitForTimeout(500);

    // 3. Click on Bin A-01-01A (code 01A) to open Detail Drawer
    const binA01A = page.locator('button:has-text("01A")').first();
    await binA01A.click({ force: true });
    await page.waitForTimeout(800);

    // Verify Drawer opened
    await expect(page.getByText('Chi tiết vị trí ô ngăn')).toBeVisible();
    await expect(page.getByRole('heading', { name: /ASUS ROG Strix GeForce RTX 4090/i })).toBeVisible();

    // Screenshot 2: Drawer Open with Serials
    await page.screenshot({ path: 'test-results/admin-warehouse-map-drawer.png' });

    // Close Drawer
    const closeBtn = page.locator('button:has-text("Đóng")').first();
    if (await closeBtn.isVisible()) {
      await closeBtn.click();
    }
    await page.waitForTimeout(400);

    // 4. Toggle Managing mode
    const manageBtn = page.locator('button:has-text("Quản lý vị trí kho")').first();
    if (await manageBtn.isVisible()) {
      await manageBtn.click();
      await page.waitForTimeout(400);
      await expect(page.getByRole('button', { name: '+ Thêm phân khu' })).toBeVisible();
    }

    // Screenshot 3: Relocate / Managing Mode Active
    await page.screenshot({ path: 'test-results/admin-warehouse-map-relocate.png' });
  });
});
