import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

const UI = process.env.E2E_UI_URL || 'http://localhost:4200';
const SCREENSHOT_DIR = path.resolve(__dirname, '../../test-results/ux-audit');

// Đảm bảo thư mục lưu ảnh tồn tại
test.beforeAll(async () => {
  if (!fs.existsSync(SCREENSHOT_DIR)) {
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  }
});

test.describe('UX/UI Comprehensive Audit', () => {

  // Helper kiểm tra tràn ngang (Horizontal Overflow)
  async function checkHorizontalOverflow(page: any, pageName: string) {
    const hasOverflow = await page.evaluate(() => {
      return document.documentElement.scrollWidth > window.innerWidth;
    });
    const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
    const innerWidth = await page.evaluate(() => window.innerWidth);
    console.log(`[Overflow Check] ${pageName}: scrollWidth=${scrollWidth}, innerWidth=${innerWidth}, overflow=${hasOverflow}`);
    return { hasOverflow, scrollWidth, innerWidth };
  }

  // Helper kiểm tra ảnh vỡ (Broken images)
  async function checkBrokenImages(page: any, pageName: string) {
    const broken = await page.evaluate(() => {
      const imgs = Array.from(document.querySelectorAll('img'));
      return imgs
        .filter(img => !img.complete || img.naturalWidth === 0)
        .map(img => img.src || img.getAttribute('src') || 'unknown');
    });
    console.log(`[Image Check] ${pageName}: found ${broken.length} broken images`);
    return broken;
  }

  test('1. Desktop UX Audit (1920x1080) - Home, Catalog, Detail, Cart, Builder', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });

    const consoleErrors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') consoleErrors.push(msg.text());
    });

    // 1.1 Home Page
    await page.goto(`${UI}/`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-01-home.png'), fullPage: false });
    const homeOverflow = await checkHorizontalOverflow(page, 'Desktop Home');
    expect(homeOverflow.hasOverflow).toBe(false);

    // 1.2 Catalog / Products
    await page.goto(`${UI}/products`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-02-catalog.png'), fullPage: false });
    const catalogOverflow = await checkHorizontalOverflow(page, 'Desktop Catalog');
    expect(catalogOverflow.hasOverflow).toBe(false);

    // 1.3 Product Detail
    // Click vào card sản phẩm đầu tiên hoặc goto trực tiếp
    const firstProduct = page.locator('app-product-card a').first();
    if (await firstProduct.count() > 0) {
      await firstProduct.click();
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(1200);
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-03-product-detail.png'), fullPage: false });
    }

    // 1.4 Cart Page
    await page.goto(`${UI}/cart`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-04-cart.png'), fullPage: false });

    // 1.5 PC Builder
    await page.goto(`${UI}/pc-builder`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-05-pc-builder.png'), fullPage: false });

    // 1.6 Login Page
    await page.goto(`${UI}/auth/login`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(800);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'desktop-06-login.png'), fullPage: false });

    console.log(`Desktop Console Errors count: ${consoleErrors.length}`);
  });

  test('2. Mobile UX Audit (iPhone 13 - 390x844) - Responsive, Drawer, Overflow, Touch Targets', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });

    // 2.1 Mobile Home
    await page.goto(`${UI}/`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'mobile-01-home.png'), fullPage: false });

    const mobileHomeOverflow = await checkHorizontalOverflow(page, 'Mobile Home');
    // Kiểm tra không tràn ngang giao diện
    expect(mobileHomeOverflow.hasOverflow).toBe(false);

    // 2.2 Hamburger / Mobile Navigation Menu
    const menuBtn = page.locator('button[aria-label*="menu" i], button[aria-label*="Menu" i], header button:has(svg), header .mobile-menu-btn').first();
    if (await menuBtn.count() > 0 && await menuBtn.isVisible()) {
      await menuBtn.click();
      await page.waitForTimeout(800);
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'mobile-02-drawer-menu.png'), fullPage: false });
      // Đóng menu
      await page.keyboard.press('Escape');
    }

    // 2.3 Mobile Catalog
    await page.goto(`${UI}/products`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'mobile-03-catalog.png'), fullPage: false });
    const mobileCatalogOverflow = await checkHorizontalOverflow(page, 'Mobile Catalog');
    expect(mobileCatalogOverflow.hasOverflow).toBe(false);

    // 2.4 Mobile Product Detail
    const mobileCard = page.locator('app-product-card a').first();
    if (await mobileCard.count() > 0) {
      await mobileCard.click();
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(1000);
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'mobile-04-product-detail.png'), fullPage: false });
      const mobileDetailOverflow = await checkHorizontalOverflow(page, 'Mobile Detail');
      expect(mobileDetailOverflow.hasOverflow).toBe(false);
    }

    // 2.5 Mobile Cart
    await page.goto(`${UI}/cart`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'mobile-05-cart.png'), fullPage: false });
    const mobileCartOverflow = await checkHorizontalOverflow(page, 'Mobile Cart');
    expect(mobileCartOverflow.hasOverflow).toBe(false);
  });

  test('3. UX Edge States - Empty Cart, Search No Results, Form Validation Errors', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });

    // 3.1 Empty Cart State
    // Xóa giỏ hoặc vào với token mới
    await page.context().clearCookies();
    await page.goto(`${UI}/cart`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'ux-edge-01-empty-cart.png') });

    // 3.2 Search Empty / Not Found State
    await page.goto(`${UI}/products?search=khongtontaisanphamnaonhuthainay99999`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'ux-edge-02-search-empty.png') });

    // 3.3 Form Validation Error State
    await page.goto(`${UI}/auth/login`);
    await page.waitForLoadState('networkidle');
    await page.locator('#email').fill('khongphaiemailhoply');
    await page.locator('#password').fill('123'); // Quá ngắn / chưa đạt policy
    await page.locator('#email').blur();
    await page.locator('#password').blur();
    await page.waitForTimeout(500);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'ux-edge-03-form-validation.png') });

    const submitBtn = page.locator('form button[type="submit"]');
    // Verify UX: Nút submit phải bị vô hiệu hóa khi form chưa hợp lệ
    await expect(submitBtn).toBeDisabled();
  });

  test('4. UX Accessibility & Interactive Touch Target Metrics', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(`${UI}/`);
    await page.waitForLoadState('networkidle');

    // Thu thập touch target sizes của tất cả buttons trên mobile
    const buttonMetrics = await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button, a[role="button"]'));
      return btns.map(b => {
        const rect = b.getBoundingClientRect();
        return {
          text: (b.textContent || '').trim().slice(0, 30),
          width: Math.round(rect.width),
          height: Math.round(rect.height),
          ariaLabel: b.getAttribute('aria-label') || ''
        };
      }).filter(b => b.width > 0 && b.height > 0);
    });

    console.log(`Evaluated ${buttonMetrics.length} buttons on mobile home page.`);
    const smallTargets = buttonMetrics.filter(b => b.width < 32 || b.height < 32);
    console.log(`Small targets (<32px): ${smallTargets.length}`, smallTargets.slice(0, 5));

    // Broken images check trên trang chủ
    const broken = await checkBrokenImages(page, 'Mobile Home');
    console.log(`Broken images on home: ${broken.length}`);
  });

});
