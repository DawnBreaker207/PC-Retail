import { test, expect } from '@playwright/test';
import { ADMIN } from './auth.helper';

test.describe('Dark Mode & Header Synchronization Review', () => {
  test.beforeEach(async ({ page }) => {
    const token = ADMIN();
    // Enable dark mode via localStorage and html class
    await page.addInitScript((jwt: string) => {
      localStorage.setItem('theme', 'dark');
      localStorage.setItem('pc-retail-theme', 'dark');
      document.documentElement.classList.add('dark');

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

  test('1. Storefront Header & Mega Menu in Dark Mode', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/');
    await page.waitForLoadState('domcontentloaded');
    await page.waitForTimeout(600);

    // Verify dark class is applied
    const isDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    expect(isDark).toBe(true);

    // Screenshot top header in dark mode
    await page.screenshot({ path: 'design-review/dark-mode-header.png', clip: { x: 0, y: 0, width: 1440, height: 160 } });

    // Hover on category in mega menu to trigger dropdown
    const catLink = page.locator('nav a:has-text("Laptop")').first();
    if (await catLink.isVisible()) {
      await catLink.hover();
      await page.waitForTimeout(400);
      await page.screenshot({ path: 'design-review/dark-mode-mega-menu.png', clip: { x: 0, y: 0, width: 1440, height: 500 } });
    }

    // Screenshot full homepage in dark mode
    await page.screenshot({ path: 'design-review/dark-mode-home.png' });
  });

  test('2. Admin Navbar & Sidebar in Dark Mode', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/admin');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1200);

    await page.screenshot({ path: 'design-review/dark-mode-admin-dashboard.png', fullPage: true });
  });

  test('3. Warehouse Map in Dark Mode & Bin Drawer', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('http://localhost:4200/admin/inventory');
    await page.waitForLoadState('domcontentloaded');
    await page.waitForTimeout(600);

    // Screenshot warehouse map in dark mode
    await page.screenshot({ path: 'design-review/dark-mode-warehouse-map.png', fullPage: true });

    // Open Drawer on first bin
    const binButton = page.locator('button:has-text("01A")').first();
    if (await binButton.isVisible()) {
      await binButton.click();
      await page.waitForTimeout(500);
      await page.screenshot({ path: 'design-review/dark-mode-warehouse-drawer.png' });
    }
  });
});
