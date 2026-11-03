import { test, expect } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';
import { ADMIN } from './auth.helper';

const UI = process.env.E2E_UI_URL || 'http://localhost:4200';
const SCREENSHOT_DIR = path.resolve(__dirname, '../../test-results/admin-kanban');

test.beforeAll(async () => {
  if (!fs.existsSync(SCREENSHOT_DIR)) {
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  }
});

async function loginAsAdmin(page: any) {
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
  await page.goto(`${UI}/admin/orders`, { waitUntil: 'networkidle' });
}

test('Admin Order Management: Switch between Table and Drag-and-Drop Kanban Board', async ({ page }) => {
  await page.setViewportSize({ width: 1600, height: 900 });

  // 1. Đăng nhập với quyền Admin và vào trang Quản lý đơn hàng
  await loginAsAdmin(page);
  await page.waitForTimeout(1000);

  // 2. Chụp ảnh màn hình chế độ Table View mặc định
  const tableViewShot = path.join(SCREENSHOT_DIR, '01-admin-orders-table.png');
  await page.screenshot({ path: tableViewShot });
  console.log('Saved Table View Screenshot to:', tableViewShot);

  // 3. Chuyển sang chế độ Kanban Kéo Thả
  const kanbanToggleBtn = page.locator('button', { hasText: 'Kanban Kéo Thả' });
  await expect(kanbanToggleBtn).toBeVisible({ timeout: 5000 });
  await kanbanToggleBtn.click();
  await page.waitForTimeout(1000);

  // 4. Chụp ảnh màn hình chế độ Kanban Board
  const kanbanViewShot = path.join(SCREENSHOT_DIR, '02-admin-orders-kanban.png');
  await page.screenshot({ path: kanbanViewShot });
  console.log('Saved Kanban View Screenshot to:', kanbanViewShot);

  // 5. Kiểm tra các cột Kanban xuất hiện
  const kanbanColumns = page.locator('.w-76');
  await expect(kanbanColumns.first()).toBeVisible({ timeout: 5000 });
  const colCount = await kanbanColumns.count();
  console.log(`Kanban columns count: ${colCount}`);
  expect(colCount).toBeGreaterThanOrEqual(4);

  // 6. Thử nghiệm kéo thả nếu có đơn hàng
  const draggableCard = page.locator('[cdkDrag]').first();
  if (await draggableCard.count() > 0 && await draggableCard.isVisible()) {
    console.log('Found draggable order card, testing drag-and-drop...');
    const targetDropList = page.locator('#kb-processing, #kb-shipped').first();
    await draggableCard.dragTo(targetDropList);
    await page.waitForTimeout(1200);

    const postDropShot = path.join(SCREENSHOT_DIR, '03-admin-orders-dragged.png');
    await page.screenshot({ path: postDropShot });
    console.log('Saved Post-Drag Screenshot to:', postDropShot);
  }
});
