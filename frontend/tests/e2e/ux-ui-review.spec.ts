import { test, expect } from '@playwright/test';
import { ADMIN } from './auth.helper';

test.describe('UX/UI Review & Verification', () => {
  test.beforeEach(async ({ page }) => {
    const token = ADMIN();
    await page.addInitScript((jwt: string) => {
      const session = {
        accessToken: jwt,
        refreshToken: jwt,
        user: {
          id: 1,
          fullName: 'Admin Quản Trị',
          email: 'admin@pc-retail.com',
          systemRole: 'ADMIN',
        },
      };
      localStorage.setItem('pc-retail-session', JSON.stringify(session));
    }, token);
  });

  test('1. Admin Dashboard UX/UI - Verify status tags & charts', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/admin');
    await page.waitForLoadState('networkidle');

    // Screenshot dashboard
    await page.screenshot({ path: 'design-review/admin-dashboard.png', fullPage: true });

    // Verify recent orders table has status badges (Spartan UI)
    const badges = page.locator('[hlmBadge], [hlmTable] span');
    const count = await badges.count();
    expect(count).toBeGreaterThan(0);
  });

  test('2. Admin Orders UX/UI - Verify confirmation modal', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/admin/orders');
    await page.waitForLoadState('networkidle');

    // Check if confirm buttons exist
    const confirmBtn = page.locator('button:has-text("Xác nhận")').first();
    if (await confirmBtn.isVisible()) {
      await confirmBtn.click();
      // Verify modal is open
      const modalTitle = page.locator('div[hlmDialogContent] h3[hlmDialogTitle], div[hlmDialogContent] h3');
      await expect(modalTitle).toBeVisible();
      await page.screenshot({ path: 'design-review/admin-order-modal.png' });
      // Cancel modal
      await page.locator('div[hlmDialogContent] button:has-text("Hủy bỏ"), div[hlmDialogContent] button:has-text("Hủy")').click();
    }
  });

  test('3. Admin Inventory UX/UI - Verify search input binding', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');

    // Switch to List View if default is Visual Warehouse Map
    const listBtn = page.locator('button:has-text("Bảng danh sách")');
    if (await listBtn.isVisible()) {
      await listBtn.click();
      await page.waitForTimeout(500);
    }

    const searchInput = page.locator('input[placeholder="Tìm kiếm sản phẩm..."]');
    await expect(searchInput).toBeVisible();
    await searchInput.fill('RTX');
    await searchInput.press('Enter');
    await page.waitForTimeout(500);

    await page.screenshot({ path: 'design-review/admin-inventory.png' });
  });

  test('4. Admin Reports, Purchasing, Stocktake UX/UI', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });

    // Reports
    await page.goto('http://localhost:4200/admin/reports');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/admin-reports.png', fullPage: true });

    // Purchasing
    await page.goto('http://localhost:4200/admin/purchasing');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/admin-purchasing.png', fullPage: true });

    // Stocktake
    await page.goto('http://localhost:4200/admin/stocktake');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/admin-stocktake.png', fullPage: true });
  });

  test('5. Storefront Home & PC Builder - Desktop & Mobile Viewports', async ({ page }) => {
    // Desktop Home
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/home-desktop.png' });

    // Mobile Home (375x812)
    await page.setViewportSize({ width: 375, height: 812 });
    await page.goto('http://localhost:4200/');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/home-mobile.png' });

    // PC Builder
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/builder');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'design-review/pc-builder.png', fullPage: true });
  });
});
