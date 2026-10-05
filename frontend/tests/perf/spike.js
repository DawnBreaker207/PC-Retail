import http from 'k6/http';
import { check, sleep } from 'k6';
import { CUSTOMER } from './jwt.js';

// SPIKE: idle -> burst 80 VU 30s -> idle. Quan sát hồi phục (error về 0, p95 hạ).
export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '30s', target: 80 },
    { duration: '1m', target: 5 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
  },
};

const BASE = __ENV.E2E_BASE_URL || 'http://localhost:8888/api/v1';

export default function () {
  const res = http.get(`${BASE}/skus?page=0&size=10`, {
    headers: { Authorization: `Bearer ${CUSTOMER()}` },
  });
  check(res, { 'status 200': (x) => x.status === 200 });
  sleep(0.2);
}
