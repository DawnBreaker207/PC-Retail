// FULL-FLOW trong tab Chrome BẠN ĐANG MỞ (không mở browser mới).
//
// 1) Mở Chrome với remote debugging (làm 1 lần, giữ nguyên cửa sổ đó):
//      "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --user-data-dir="%TEMP%\chrome-debug"
// 2) Chạy (đứng ở D:\Code\PC-Retail\frontend):
//      node tests/visual/watch-flow.mjs
//    Tab mới sẽ bật trong Chrome của bạn và tự đi chợ. Đổi CDP:
//      node tests/visual/watch-flow.mjs http://localhost:9222
//
// Lưu ý: dùng profile riêng (%TEMP%\chrome-debug) nên là guest sạch, không động vào profile thật.

import { chromium } from 'playwright';

const CDP = process.argv[2] || 'http://localhost:9222';
const UI = process.env.E2E_UI_URL || 'http://localhost:4200';
const STEP_MS = 900;

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function narrate(page, text) {
  console.log('>> ' + text);
  await page.evaluate((t) => {
    let el = document.getElementById('e2e-narrator');
    if (!el) {
      el = document.createElement('div');
      el.id = 'e2e-narrator';
      el.style.cssText =
        'position:fixed;left:50%;bottom:18px;transform:translateX(-50%);z-index:99999;' +
        'background:#111;color:#fff;font:600 15px/1.4 system-ui;padding:12px 20px;border-radius:12px;' +
        'box-shadow:0 8px 30px rgba(0,0,0,.35);max-width:90vw;text-align:center';
      document.body.appendChild(el);
    }
    el.textContent = t;
  }, text);
  await sleep(STEP_MS);
}

let browser;
try {
  browser = await chromium.connectOverCDP(CDP, { slowMo: 500 });
} catch {
  console.error(
    `Không nối được Chrome ở ${CDP}.\nMở Chrome với remote debugging trước:\n` +
      `"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe" --remote-debugging-port=9222 --user-data-dir="%TEMP%\\chrome-debug"`,
  );
  process.exit(1);
}

const ctx = browser.contexts()[0] ?? (await browser.newContext());
const page = await ctx.newPage();

// 1. Home
await page.goto(`${UI}/`);
await page.waitForTimeout(2000);
await narrate(page, 'Bước 1/7: Trang chủ DAWN — header, danh mục, banner');

// 2. Products: mở đúng món còn hàng thật (mock id 1 == kho thật còn hàng).
// Các card khác có thể 422 (mock id không có tồn) — toast sẽ hiện, đó cũng là demo tốt.
await page.goto(`${UI}/products`);
await page.waitForTimeout(2500);
await narrate(page, 'Bước 2/7: Danh sách sản phẩm — mở món còn hàng (Gigabyte A16)');
await page
  .locator('app-product-card', { hasText: 'CMHH2VN893SH' })
  .first()
  .locator('a[href*="/products/"]')
  .first()
  .click();
await page.waitForTimeout(2500);
await narrate(page, 'Bước 3/7: Chi tiết sản phẩm — giá, tồn kho, đánh giá');
await page.screenshot({ path: 'test-results/watch-detail.png' });
await page.locator('button', { hasText: 'Thêm vào giỏ' }).first().click();
await page.waitForTimeout(1500);
await narrate(page, 'Đã bấm thêm vào giỏ — badge giỏ hàng tăng');

// 3. Cart
await page.goto(`${UI}/cart`);
await page.waitForTimeout(2000);
await narrate(page, 'Bước 4/7: Giỏ hàng — kiểm món, số lượng');

// 4. Checkout: form giao hàng
await page.goto(`${UI}/checkout`);
await page.waitForTimeout(2000);
await narrate(page, 'Bước 5/7: Đặt hàng — điền thông tin + xác minh SĐT bằng OTP');
const watchPhone = `09${Math.floor(10000000 + Math.random() * 89999999)}`;
await page.locator('#fullName').fill('Khach Demo');
await page.locator('#phone').fill(watchPhone);
await page.locator('#city').click();
await page.waitForTimeout(600);
await page.locator('.ant-select-item-option').first().click();
await page.locator('#district').fill('Hai Ba Trung');
await page.locator('#street').fill('123 Duong Demo');
await page.getByRole('button', { name: /gửi mã otp/i }).click();
await page.waitForTimeout(800);
const qText = (await page.locator('text=/\\d+ \\+ \\d+/').first().textContent()) ?? '';
const qm = qText.match(/(\d+)\s*\+\s*(\d+)/);
await page.locator('#challengeAnswer').fill(String(Number(qm[1]) + Number(qm[2])));
await narrate(page, 'Giải captcha chống bot rồi bấm Gửi mã');
await page.getByRole('button', { name: /gửi mã otp/i }).click();
await page.waitForTimeout(800);
const dbgCode = await fetch(`http://localhost:8888/api/v1/auth/otp/debug/${watchPhone}`)
  .then((r) => r.json()).then((b) => b.data.code);
await page.locator('#otpCode').fill(dbgCode);
await page.getByRole('button', { name: /xác minh/i }).click();
await page.waitForTimeout(800);
await narrate(page, 'SĐT đã xác minh — bấm Tiếp tục');
await page.getByRole('button', { name: /tiếp tục/i }).click();
await page.waitForTimeout(2000);

// 5. Review + đặt hàng
await narrate(page, 'Bước 5/7: Xác nhận đơn — kiểm tiền ship, voucher, tổng');
await page.screenshot({ path: 'test-results/watch-review.png' });
await page.getByRole('button', { name: /đặt hàng/i }).click();
await page.waitForTimeout(3000);

// 6. Kết quả: guest dừng ở "Đã nhận đơn, chờ thanh toán" (đúng docs: guest không trả online)
await narrate(page, 'Bước 6/7: Đã nhận đơn, chờ thanh toán (guest trả khi nhận hàng)');
await page.screenshot({ path: 'test-results/watch-result.png' });
await narrate(page, 'Xong full luồng guest: xem hàng → thêm giỏ → đặt đơn. Tab giữ nguyên cho bạn xem.');
console.log('DONE. Tab vẫn mở trong Chrome của bạn.');
// Không đóng browser/page — để bạn tự xem.
process.exit(0);
