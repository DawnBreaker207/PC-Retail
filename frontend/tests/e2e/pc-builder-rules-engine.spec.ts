import { test, expect } from '@playwright/test';

test.describe('PC Builder 3-Tier Rules Engine (SAFE / WARNING / BLOCK)', () => {

  test('1. Custom Build loads with Empty State message', async ({ page }) => {
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.message));

    await page.goto('http://localhost:4200/pc-builder/custom', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Verify Title and Empty State message
    const title = page.getByText(/Cấu hình dự tính/i).first();
    await expect(title).toBeVisible();

    const emptyMsg = page.getByText(/Chưa có linh kiện nào được chọn/i).first();
    await expect(emptyMsg).toBeVisible();

    await page.screenshot({ path: 'test-results/pc-builder-empty-state.png' });
  });

  test('2. Product Picker Modal opens, selects component and shows Wattage HUD & Rules Engine', async ({ page }) => {
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.message));

    await page.goto('http://localhost:4200/pc-builder/custom', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Click on the first "+ Chọn linh kiện" button
    const pickButtons = page.getByRole('button', { name: /Chọn linh kiện/i });
    expect(await pickButtons.count()).toBeGreaterThan(0);
    await pickButtons.first().click();
    await page.waitForTimeout(800);

    // Verify Modal opens with Heading "Chọn linh kiện"
    const modalHeading = page.getByRole('heading', { name: 'Chọn linh kiện' });
    await expect(modalHeading).toBeVisible();

    // Pick first item in modal
    const firstSkuCard = page.locator('app-product-picker-modal .group').first();
    await expect(firstSkuCard).toBeVisible();
    await firstSkuCard.click();
    await page.waitForTimeout(1000);

    // Verify Wattage HUD is now displayed!
    await expect(page.getByText(/Ước tính tiêu thụ/i)).toBeVisible();
    await expect(page.getByText(/Khuyến nghị nguồn/i)).toBeVisible();

    // Verify 1 component selected
    await expect(page.getByText(/1 linh kiện/i).first()).toBeVisible();

    // Log the entire text of app-build-summary
    const summaryText = await page.locator('app-build-summary').innerText();
    console.log('BUILD SUMMARY INNER TEXT:\n', summaryText);

    // Verify at least one compatibility rule card is rendered
    const ruleCard = page.getByText(/\[SAFE\]|\[KHUYẾN NGHỊ\]|\[BLOCK\]/i).first();
    await expect(ruleCard).toBeVisible({ timeout: 5000 });

    await page.screenshot({ path: 'test-results/pc-builder-rules-engine-active.png', fullPage: true });
  });
});
