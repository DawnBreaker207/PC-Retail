import { test } from '@playwright/test';
import { createHmac } from 'node:crypto';

test.use({ viewport: { width: 1440, height: 900 } });

// JWT thật ký bằng infra/.env secret (user 1 ADMIN có trong DB black-box)
function realAdminToken(): string {
  const secret = Buffer.from(
    'RGF3bkJyZWFrZXJEYXduQnJlYWtlckRhd25CcmVha2Vy',
    'base64',
  );
  const now = Math.floor(Date.now() / 1000);
  const b64 = (s: string) =>
    Buffer.from(s).toString('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  const data =
    `${b64(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))}.` +
    `${b64(
      JSON.stringify({
        sub: 'visual',
        id: 1,
        username: 'visual',
        email: 'visual@t.vn',
        role: 'ADMIN',
        iat: now,
        exp: now + 3600,
      }),
    )}`;
  const sig = createHmac('sha256', secret)
    .update(data)
    .digest('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `${data}.${sig}`;
}

test('visual: home + compare + reports', async ({ page }) => {
  await page.goto('http://localhost:4200/');
  await page.waitForTimeout(2500);
  await page.screenshot({ path: 'design-review/home-actual.png' });

  await page.goto('http://localhost:4200/products/compare?ids=1,2');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: 'design-review/compare-actual.png' });

  // session thật (BE thật qua nginx proxy) — mock token sẽ 401 và bị logout
  const token = realAdminToken();
  await page.goto('http://localhost:4200/auth/login');
  await page.waitForTimeout(1000);
  await page.evaluate((t) => {
    const accessToken = t;
    localStorage.setItem(
      'pc-retail-session',
      JSON.stringify({
        accessToken,
        refreshToken: accessToken,
        user: { id: 1, fullName: 'Admin', email: 'admin@test.vn', phone: '', systemRole: 'ADMIN' },
      }),
    );
  }, token);
  await page.goto('http://localhost:4200/admin/reports');
  await page.waitForTimeout(2500);
  await page.screenshot({ path: 'design-review/reports-actual.png' });
});
