import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { ADMIN, CUSTOMER, uuid } from './jwt.js';

const serverErrors = new Counter('server_errors_5xx');
const conflict409 = new Counter('conflict_409');
const stock422 = new Counter('stock_422');
const other4xx = new Counter('other_4xx');

export const options = {
  vus: 20,
  duration: '2m',
  thresholds: {
    // 409 xung đột giỏ chung 1 user là chấp nhận được dưới tải ghi;
    // 500 mới là lỗi thật -> phải bằng 0
    http_req_failed: ['rate<0.05'],
    server_errors_5xx: ['count==0'],
    http_req_duration: ['p(95)<1500'],
  },
};

const BASE = __ENV.E2E_BASE_URL || 'http://localhost:8888/api/v1';

export function setup() {
  // Nạp đạn tồn trước khi bắn (giữ > 50 để checkout không 422)
  const inv = http.get(`${BASE}/inventory?page=0&size=10&skuId=2`, {
    headers: { Authorization: `Bearer ${ADMIN()}` },
  });
  const item = inv.json().data.content[0];
  if (item.availableQuantity < 50) {
    http.put(
      `${BASE}/inventory/2/restock`,
      JSON.stringify({ quantity: 100, unitCost: 100000 }),
      {
        headers: {
          Authorization: `Bearer ${ADMIN()}`,
          'Content-Type': 'application/json',
        },
      },
    );
  }
}

function track(res) {
  if (res.status >= 500) serverErrors.add(1);
  else if (res.status === 409) conflict409.add(1);
  else if (res.status === 422) stock422.add(1);
  else if (res.status >= 400) other4xx.add(1);
  return res;
}

function reads(token) {
  const h = { headers: { Authorization: `Bearer ${token}` } };
  const r = Math.random();
  let res;
  if (r < 0.3) res = track(http.get(`${BASE}/cart`, h));
  else if (r < 0.5)
    res = track(
      http.get(`${BASE}/inventory?page=0&size=10`, {
        headers: { Authorization: `Bearer ${ADMIN()}` },
      }),
    );
  else if (r < 0.7) res = track(http.get(`${BASE}/promotion/flash-sales/active`, h));
  else if (r < 0.85) res = track(http.get(`${BASE}/skus?page=0&size=10`, h));
  else
    res = track(
      http.post(
        `${BASE}/vouchers/validate`,
        JSON.stringify({ code: 'NOPE-LOAD', orderTotal: 20000000 }),
        { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } },
      ),
    );
  check(res, { 'read ok': (x) => x.status === 200 });
}

function buyAndCancel(token) {
  const h = { headers: { Authorization: `Bearer ${token}` } };
  const j = { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } };
  track(http.post(`${BASE}/cart/items`, JSON.stringify({ skuId: 2, quantity: 1 }), j));
  const co = track(
    http.post(
      `${BASE}/orders/checkout`,
      JSON.stringify({ idempotencyKey: uuid(), fulfillmentType: 'PICKUP', shippingAddress: {} }),
      j,
    ),
  );
  if (co.status !== 200) return;
  const subId = co.json().subOrders[0].id;
  const cancel = track(http.post(`${BASE}/orders/sub-orders/${subId}/cancel`, null, h));
  check(cancel, { 'cancel ok': (x) => x.status === 200 });
}

export default function () {
  const token = CUSTOMER();
  // 90 đọc / 10 ghi: tỉ lệ thực tế + tránh 20 VU giẫm 1 dòng giỏ (409 là bình thường)
  if (Math.random() < 0.9) reads(token);
  else buyAndCancel(token);
  sleep(0.2);
}
