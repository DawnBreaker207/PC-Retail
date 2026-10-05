import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: process.env.PW_DIR || './tests/e2e',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  // DB black-box dùng chung, tồn kho biến động theo từng test: chạy tuần tự để
  // assertion tuyệt đối (before/after) là xác định; concurrency vẫn được phủ
  // bởi test race bên trong (Promise.all) chứ không phải bằng worker song song
  workers: 1,
  // Xem lại bằng mắt: HTML report + trace/screenshot khi fail
  //   npx playwright test --reporter=html && npx playwright show-report
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL: (process.env.E2E_BASE_URL || 'http://localhost:8888/api/v1') + '/',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
    // Chạy headed xem thao tác: npx playwright test --headed
    // Chậm lại để nhìn rõ: PW_SLOWMO=400 (ms)
    launchOptions: { slowMo: Number(process.env.PW_SLOWMO ?? 0) },
  },
  retries: 1,
});
