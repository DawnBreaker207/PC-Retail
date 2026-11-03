import { test, expect } from '@playwright/test';
import { ADMIN } from './auth.helper';

test.describe('Advanced Warehouse Map & Header Review', () => {
  test.beforeEach(async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });

    // Real admin session
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
  });

  test('1. Default Zone View (Thoáng mắt, tập trung từng phân khu)', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Verify Default View is Warehouse Map and Zone View
    const mapHeading = page.locator('text=Quản lý kho & Tồn kho');
    await expect(mapHeading).toBeVisible();

    await page.screenshot({ path: 'design-review/warehouse-map-zone-view.png', fullPage: true });
  });

  test('2. Overview View (Toàn cảnh mặt bằng có Cổng kho & Lối đi)', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Click "Toàn cảnh kho" button
    const overviewBtn = page.locator('button:has-text("Toàn cảnh kho")');
    await overviewBtn.click();
    await page.waitForTimeout(400);

    await page.screenshot({ path: 'design-review/warehouse-map-overview.png', fullPage: true });
  });

  test('3. Shelf Zoom View (Cận cảnh 1 Kệ với kích thước lớn)', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Click on Shelf 01 name to zoom
    const shelfLink = page.locator('button:has-text("Kệ 01")').first();
    await shelfLink.click();
    await page.waitForTimeout(400);

    // Verify Zoom View
    await expect(page.locator('text=Quay lại toàn cảnh kho')).toBeVisible();
    await page.screenshot({ path: 'design-review/warehouse-map-shelf-zoom.png', fullPage: true });

    // Click back
    await page.locator('button:has-text("Quay lại toàn cảnh kho")').click();
    await page.waitForTimeout(300);
  });

  test('4. Managing Mode (Bật Quản lý vị trí, Thêm Phân khu, Thêm Ô)', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Toggle Managing Mode
    const manageBtn = page.locator('button:has-text("Quản lý vị trí kho")');
    await manageBtn.click();
    await page.waitForTimeout(400);

    // Click "+ Thêm phân khu"
    const addZoneBtn = page.locator('button:has-text("Thêm phân khu")');
    if (await addZoneBtn.isVisible()) {
      await addZoneBtn.click();
      await page.waitForTimeout(400);
    }

    await page.screenshot({ path: 'design-review/warehouse-map-managing-mode.png', fullPage: true });
  });

  test('5. Relocation Modal with 20s Countdown Timer', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Open Bin 01A Drawer first
    const binButton = page.locator('button:has-text("01A")').first();
    await binButton.click();
    await page.waitForTimeout(400);

    // Click "Điều chuyển hàng hóa từ ô này"
    const relocateBtn = page.locator('button:has-text("Điều chuyển hàng hóa từ ô này")');
    await relocateBtn.click();
    await page.waitForTimeout(500);

    // Verify Relocate Modal with Countdown Timer
    await expect(page.locator('text=Tự động hủy an toàn sau:')).toBeVisible();
    await page.screenshot({ path: 'design-review/warehouse-map-relocation-modal.png' });

    // Cancel modal
    await page.locator('button:has-text("Hủy bỏ")').click();
  });

  test('6. Bin Detail Drawer with Serial Numbers & Max Capacity', async ({ page }) => {
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(600);

    // Click Bin 01A
    const binButton = page.locator('button:has-text("01A")').first();
    await binButton.click();
    await page.waitForTimeout(500);

    // Expand Serial numbers if available
    const serialToggle = page.locator('button:has-text("Mã Serial chi tiết")').first();
    if (await serialToggle.isVisible()) {
      await serialToggle.click();
      await page.waitForTimeout(300);
    }

    await page.screenshot({ path: 'design-review/warehouse-map-drawer-serials.png' });
  });
});
