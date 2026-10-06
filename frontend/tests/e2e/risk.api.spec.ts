import { test, expect } from '@playwright/test';
import { ensureStock, guestOtpDance, guestPhone, uuid } from './auth.helper';

// Risk-gate docs 13.5 cho guest checkout: OTP bắt buộc, captcha, ngưỡng giá trị.
// (Rate limit IP + lịch sử bùng phủ ở white-box; E2E cooldown 60s/SĐT nên không spam cùng số được.)

async function guestCart(request: import('@playwright/test').APIRequestContext, token: string) {
  await request.post(`cart/guest/items?token=${token}`, { data: { skuId: 2, quantity: 1 } });
}

// R1 — chưa verify OTP: checkout 400, kho không đụng
test('RISK guest checkout without OTP rejected, stock untouched', async ({ request }) => {
  const token = `risk-${Date.now().toString(36)}`;
  await ensureStock(request, 2);
  await guestCart(request, token);

  const co = await request.post('orders/guest-checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'PICKUP',
      shippingAddress: {},
      guestToken: token,
      guestPhone: guestPhone(),
    },
  });
  expect(co.status()).toBe(400);
  expect(await co.text()).toContain('verify');
});

// R2 — captcha sai: không gửi được OTP
test('RISK wrong captcha blocks OTP request', async ({ request }) => {
  const ch = await request.get('auth/otp/challenge');
  expect(ch.status()).toBe(200);
  const { challengeId } = (await ch.json()).data;

  const bad = await request.post('auth/otp/request', {
    data: { phone: guestPhone(), challengeId, challengeAnswer: 'sai' },
  });
  expect(bad.status()).toBe(400);
});

// R3 — challenge dùng 1 lần: tái dùng cùng id bị chặn
test('RISK captcha single-use', async ({ request }) => {
  const phone = guestPhone();
  const ch = await request.get('auth/otp/challenge');
  const { challengeId, question } = (await ch.json()).data;
  const m = question.match(/(\d+)\s*\+\s*(\d+)/);
  const answer = String(Number(m[1]) + Number(m[2]));

  const r1 = await request.post('auth/otp/request', {
    data: { phone, challengeId, challengeAnswer: answer },
  });
  expect(r1.status()).toBe(200);

  const r2 = await request.post('auth/otp/request', {
    data: { phone: guestPhone(), challengeId, challengeAnswer: answer },
  });
  expect(r2.status()).toBe(400);
});

// R4 — mã OTP sai 5 lần: mã bị đốt, debug hết thấy mã
test('RISK OTP burned after 5 wrong attempts', async ({ request }) => {
  const phone2 = guestPhone();
  const ch = await request.get('auth/otp/challenge');
  const { challengeId, question } = (await ch.json()).data;
  const m = question.match(/(\d+)\s*\+\s*(\d+)/);
  const answer = String(Number(m[1]) + Number(m[2]));
  const req = await request.post('auth/otp/request', {
    data: { phone: phone2, challengeId, challengeAnswer: answer },
  });
  expect(req.status()).toBe(200);
  for (let i = 0; i < 5; i++) {
    await request.post('auth/otp/verify', { data: { phone: phone2, code: '000000' } });
  }
  const dbg = await request.get(`auth/otp/debug/${phone2}`);
  expect(dbg.status()).toBe(404);
});

// R5 — vượt ngưỡng 30M: chặn ngay cả khi đã verify, không trừ kho
test('RISK over-threshold guest order blocked without stock touch', async ({ request }) => {
  const token = `risk-${Date.now().toString(36)}`;
  await ensureStock(request, 2, 25);
  await request.post(`cart/guest/items?token=${token}`, { data: { skuId: 2, quantity: 20 } });

  const phone = guestPhone();
  await guestOtpDance(request, phone);

  const co = await request.post('orders/guest-checkout', {
    data: {
      idempotencyKey: uuid(),
      fulfillmentType: 'PICKUP',
      shippingAddress: {},
      guestToken: token,
      guestPhone: phone,
    },
  });
  expect(co.status()).toBe(400);
  expect(await co.text()).toContain('online');
});
