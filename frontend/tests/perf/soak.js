import http from 'k6/http';
import { check, sleep } from 'k6';
import { ADMIN, CUSTOMER } from './jwt.js';

// SOAK mini 5 phút tải đều, chỉ đọc (không bào tồn).
// Soak thật cần hàng giờ + theo dõi GC/restart — đây là bản rút gọn 5 phút xem trend.
// Chạy kèm: docker stats backend --no-stream trước/sau để so RAM.
export const options = {
  vus: 10,
  duration: '5m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

const BASE = __ENV.E2E_BASE_URL || 'http://localhost:8888/api/v1';
const URLS = [
  'cart',
  'inventory?page=0&size=10',
  'promotion/flash-sales/active',
  'skus?page=0&size=10',
  'notifications',
];

export default function () {
  const u = URLS[__ITER % URLS.length];
  const token = u.startsWith('inventory') ? ADMIN() : CUSTOMER();
  const res = http.get(`${BASE}/${u}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  check(res, { 'status 200': (x) => x.status === 200 });
  sleep(0.5);
}
