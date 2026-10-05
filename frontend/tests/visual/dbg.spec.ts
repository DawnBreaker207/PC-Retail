import { test } from '@playwright/test';

test('console errors on home', async ({ page }) => {
  const errors: string[] = [];
  page.on('console', (m) => {
    if (m.type() === 'error') errors.push(m.text().substring(0, 300));
  });
  page.on('pageerror', (e) => errors.push('PAGEERROR: ' + String(e).substring(0, 300)));
  await page.goto('http://localhost:4200/', { waitUntil: 'networkidle' });
  await page.waitForTimeout(4000);
  const body = await page.locator('app-root').innerHTML();
  console.log('APP-ROOT-LEN=' + body.length);
  console.log('ERRORS=' + JSON.stringify(errors.slice(0, 10)));
});
