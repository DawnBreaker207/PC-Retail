import http from 'k6/http';
import { check, sleep } from 'k6';
import { ADMIN, CUSTOMER, uuid } from './jwt.js';
export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
};

const BASE = __ENV.E2E_BASE_URL || 'http://localhost:8888/api/v1';

export function setup() {
  // Member checkout PENDING giữ hàng — nạp đạn trước để đo cliff infra, không phải cliff tồn kho
  const inv = http.get(`${BASE}/inventory?page=0&size=10&skuId=1`, {
    headers: { Authorization: `Bearer ${ADMIN()}` },
  });
  if (inv.json().data.content[0].availableQuantity < 150) {
    http.put(
      `${BASE}/inventory/1/restock`,
      JSON.stringify({ quantity: 200, unitCost: 100000 }),
      {
        headers: {
          Authorization: `Bearer ${ADMIN()}`,
          'Content-Type': 'application/json',
        },
      },
    );
  }
}

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
    // Member checkout (không cancel — đo throughput checkout thuần; đơn PENDING dọn bởi expiry job).
    // Guest cần OTP dance nên không hợp cho burst VUs cùng IP (rate limit).
    const h = { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } };
    http.post(`${BASE}/cart/items`, JSON.stringify({ skuId: 1, quantity: 1 }), h);
    res = http.post(
      `${BASE}/orders/checkout`,
      JSON.stringify({ idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} }),
      h,
    );
  }
  check(res, { 'status ok-ish': (x) => x.status === 200 || x.status === 201 });
  sleep(0.1);
}
