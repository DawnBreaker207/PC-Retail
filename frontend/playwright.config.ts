import { defineConfig } from '@playwright/test';

// Headed by default so runs are watchable; set PW_HEADLESS=1 for headless (CI).
const headed = process.env.PW_HEADLESS !== '1' || process.argv.includes('--headed');

export default defineConfig({
  testDir: process.env.PW_DIR || './tests/e2e',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  // Shared black-box DB with per-test stock changes: run serially for
  // deterministic before/after assertions; concurrency is covered by
  // in-test races (Promise.all), not parallel workers.
  workers: 1,
  // Review visually: HTML report + trace/screenshot on failure
  //   npx playwright test --reporter=html && npx playwright show-report
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL: (process.env.E2E_BASE_URL || 'http://localhost:8080/api/v1') + '/',
    headless: !headed,
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
    // Watch the run: default headed with 500ms slow-mo.
    // Slower: PW_SLOWMO=1000. Off: PW_SLOWMO=0. Headless: PW_HEADLESS=1
    launchOptions: {
      slowMo: Number(process.env.PW_SLOWMO ?? (headed ? 500 : 0)),
    },
  },
  retries: 1,
});
