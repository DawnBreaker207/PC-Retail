import http from 'k6/http';
import { check, sleep } from 'k6';
import { ADMIN, CUSTOMER, uuid } from './jwt.js';

// STRESS: leo thang tìm điểm gãy (không đặt threshold pass/fail — quan sát cliff)
export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
};

const BASE = __ENV.E2E_BASE_URL || 'http://localhost:8888/api/v1';

export default function () {
  const token = CUSTOMER();
  const r = Math.random();
  let res;
  if (r < 0.5) {
    res = http.get(`${BASE}/skus?page=0&size=10`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  } else if (r < 0.8) {
    res = http.get(`${BASE}/inventory?page=0&size=10`, {
      headers: { Authorization: `Bearer ${ADMIN()}` },
    });
  } else {
    const t = `st-${Date.now().toString(36)}-${__VU}-${__ITER}`;
    http.post(`${BASE}/cart/guest/items?token=${t}`, JSON.stringify({ skuId: 1, quantity: 1 }), {
      headers: { 'Content-Type': 'application/json' },
    });
    res = http.post(
      `${BASE}/orders/guest-checkout`,
      JSON.stringify({
        idempotencyKey: uuid(),
        fulfillmentType: 'PICKUP',
        shippingAddress: {},
        guestToken: t,
        guestEmail: 'e2e@t.vn',
      }),
      { headers: { 'Content-Type': 'application/json' } },
    );
  }
  check(res, { 'status ok-ish': (x) => x.status === 200 || x.status === 201 });
  sleep(0.1);
}
