import { test, expect } from '@playwright/test';

// Tour chụp màn hình phục vụ audit UX (không chạy trong suite: PW_DIR=tests/e2e).
// Chạy: npx playwright test --config /dev/null ... -> dùng trực tiếp:
//   npx playwright test tests/visual/uxtour.spec.ts --project=chromium
// Đơn giản: PW_DIR=tests/visual npx playwright test
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';
const OUT = 'design-review/ux';

test.describe('ux tour desktop', () => {
  test.use({ viewport: { width: 1440, height: 900 } });

  test('screens', async ({ page }) => {
    const seen: string[] = [];
    page.on('framenavigated', (f) => {
      if (f === page.mainFrame()) seen.push(new URL(f.url()).pathname);
    });
  await page.goto(`${UI}/`);
  await page.waitForTimeout(2500);
  await page.screenshot({ path: `${OUT}/01-home.png` });

  await page.goto(`${UI}/products`);
  await page.waitForTimeout(2500);
  await page.screenshot({ path: `${OUT}/02-products.png` });

  const firstLink = page.locator('app-product-card a[href*="/products/"]').first();
  await expect(firstLink).toBeVisible({ timeout: 15000 });
  await firstLink.click();
  await page.waitForTimeout(2000);
  await page.screenshot({ path: `${OUT}/03-detail.png` });
  // eslint-disable-next-line no-console
  console.log('NAV:', JSON.stringify(seen));

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${OUT}/04-cart-empty.png` });

  // Bỏ 1 món vào giỏ rồi chụp lại cart đầy + checkout đầy (màn hình tiền)
  await page.goto(`${UI}/products`);
  await page.waitForTimeout(2000);
  await page.locator('app-product-card').first().locator('button', { hasText: 'Thêm vào giỏ' }).click();
  await page.waitForTimeout(2000);
  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${OUT}/04b-cart-full.png` });

  await page.goto(`${UI}/checkout`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: `${OUT}/05-checkout.png` });

  await page.goto(`${UI}/auth/login`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${OUT}/06-login.png` });

  await page.goto(`${UI}/tra-cuu-don-hang`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${OUT}/07-lookup.png` });

  await page.goto(`${UI}/pc-builder`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: `${OUT}/08-builder.png` });
  });
});

test.describe('ux tour mobile', () => {
  test.use({ viewport: { width: 390, height: 844 } });

  test('screens', async ({ page }) => {
  await page.goto(`${UI}/`);
  await page.waitForTimeout(2500);
  await page.screenshot({ path: `${OUT}/m01-home.png` });

  await page.goto(`${UI}/products`);
  await page.waitForTimeout(2500);
  await page.screenshot({ path: `${OUT}/m02-products.png` });

  await page.goto(`${UI}/cart`);
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${OUT}/m03-cart.png` });
  });
});
