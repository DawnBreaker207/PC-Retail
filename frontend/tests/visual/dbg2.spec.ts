import { test } from '@playwright/test';

test('debug session', async ({ page }) => {
  await page.goto('http://localhost:4200/auth/login');
  await page.waitForTimeout(1500);
  await page.evaluate(() => {
    localStorage.setItem(
      'pc-retail-session',
      JSON.stringify({
        accessToken: 'mock-token-visual',
        refreshToken: 'mock-refresh-visual',
        user: { id: 1, fullName: 'Admin', email: 'admin@dawn.vn', phone: '0912345678', systemRole: 'ADMIN' },
      }),
    );
  });
  page.on('request', (r) => {
    if (r.url().includes('/api/')) console.log('REQ ' + r.method() + ' ' + r.url().replace('http://localhost:4200', ''));
  });
  page.on('response', async (r) => {
    if (r.url().includes('/api/')) console.log('RES ' + r.status() + ' ' + r.url().replace('http://localhost:4200', ''));
  });
  await page.goto('http://localhost:4200/admin/reports');
  await page.waitForTimeout(2500);
  console.log('URL=' + page.url());
  const stored = await page.evaluate(() => localStorage.getItem('pc-retail-session')?.substring(0, 80));
  console.log('STORED=' + stored);
  const header = await page.locator('header').innerText().catch(() => 'no-header');
  console.log('HEADER=' + String(header).substring(0, 200));
});
