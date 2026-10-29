// k6 run -e BASE_URL=https://your-host -e API_USER=... -e API_PASSWORD=... scripts/load-test.js
// Read-only workload. Tune VUs and thresholds for your own hardware/SLO.
import http from 'k6/http';
import { check, sleep } from 'k6';
import encoding from 'k6/encoding';
export const options = {
  stages: [{ duration: '30s', target: 20 }, { duration: '1m', target: 100 }, { duration: '30s', target: 0 }],
  thresholds: { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000'] }
};
export default function () {
  if (!__ENV.BASE_URL || !__ENV.API_USER || !__ENV.API_PASSWORD) throw new Error('Set BASE_URL, API_USER, API_PASSWORD');
  const after = Math.floor(Math.random() * Number(__ENV.PRODUCTS || 100000));
  const r = http.get(`${__ENV.BASE_URL}/api/products?after=${after}&size=25`, {
    headers: { Authorization: `Basic ${encoding.b64encode(`${__ENV.API_USER}:${__ENV.API_PASSWORD}`)}` }
  });
  check(r, { 'products return 200': r => r.status === 200, 'page is bounded': r => r.status === 200 && r.json('content').length <= 25 });
  sleep(0.2);
}
