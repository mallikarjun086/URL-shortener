import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 200 },  // Ramp up to 200 virtual users
    { duration: '1m',  target: 1000 }, // Sustain 1,000 QPS load
    { duration: '30s', target: 0 },    // Ramp down to 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<10', 'p(99)<25'], // 95% of requests must complete within 10ms
    http_req_failed: ['rate<0.01'],              // Less than 1% failure rate
  },
};

export default function () {
  const url = 'http://localhost:8081/sys-primer';
  const res = http.get(url, { redirects: 0 });

  check(res, {
    'status is 302 or 200': (r) => r.status === 302 || r.status === 200,
  });

  sleep(0.01);
}
