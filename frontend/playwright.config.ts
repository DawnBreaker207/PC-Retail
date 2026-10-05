import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: process.env.PW_DIR || './tests/e2e',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  // DB black-box dùng chung, tồn kho biến động theo từng test: chạy tuần tự để
  // assertion tuyệt đối (before/after) là xác định; concurrency vẫn được phủ
  // bởi test race bên trong (Promise.all) chứ không phải bằng worker song song
  workers: 1,
  use: {
    baseURL: (process.env.E2E_BASE_URL || 'http://localhost:8888/api/v1') + '/',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
  },
  retries: 1,
  reporter: [['list']],
});
