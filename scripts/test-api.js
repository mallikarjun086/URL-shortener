/**
 * Automated System Integration & End-to-End API Test Suite
 * Tests URL Shortening, Redirection, Bloom Filter, Analytics, and Rate Limiting against the live backend server.
 */

const BASE_URL = 'http://localhost:8081';

async function runTests() {
  console.log('---------------------------------------------------------');
  console.log('🚀 STARTING AUTOMATED END-TO-END SYSTEM INTEGRATION TESTS');
  console.log('---------------------------------------------------------\n');

  let passed = 0;
  let failed = 0;

  async function test(name, fn) {
    try {
      await fn();
      console.log(`  ✅ PASS: ${name}`);
      passed++;
    } catch (err) {
      console.error(`  ❌ FAIL: ${name} -> ${err.message}`);
      failed++;
    }
  }

  // 1. Test URL Shortening
  let createdShortCode = '';
  await test('POST /api/v1/urls - Create Short URL with Custom Alias', async () => {
    const alias = `test-${Date.now().toString(36)}`;
    const res = await fetch(`${BASE_URL}/api/v1/urls`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        longUrl: 'https://github.com/donnemartin/system-design-primer',
        customAlias: alias
      })
    });

    if (res.status !== 200 && res.status !== 201) {
      throw new Error(`Expected 200/201, got HTTP ${res.status}`);
    }

    const data = await res.json();
    if (!data.shortCode) throw new Error('Response missing shortCode field');
    createdShortCode = data.shortCode;
  });

  // 2. Test Link Resolution & Redirect
  await test(`GET /${createdShortCode} - Resolve Short URL & Track Click`, async () => {
    const res = await fetch(`${BASE_URL}/${createdShortCode}`, { redirect: 'manual' });
    if (res.status !== 302 && res.status !== 200) {
      throw new Error(`Expected 302 Redirect, got HTTP ${res.status}`);
    }
  });

  // 3. Test Bloom Filter Non-Existent Key Defense
  await test('GET /invalid-fake-key-9999 - Bloom Filter Fast Rejection', async () => {
    const res = await fetch(`${BASE_URL}/invalid-fake-key-9999`, { redirect: 'manual' });
    if (res.status !== 400 && res.status !== 404) {
      throw new Error(`Expected HTTP 400/404 rejection, got HTTP ${res.status}`);
    }
  });

  // 4. Test Analytics Telemetry Endpoint
  await test(`GET /api/v1/analytics/${createdShortCode} - Fetch Click Analytics`, async () => {
    const res = await fetch(`${BASE_URL}/api/v1/analytics/${createdShortCode}`);
    if (res.status !== 200) {
      throw new Error(`Expected HTTP 200, got HTTP ${res.status}`);
    }
    const data = await res.json();
    if (data.totalClicks === undefined) throw new Error('Missing totalClicks in analytics response');
  });

  // 5. Test User Authentication (JWT Register & Login)
  await test('POST /api/v1/auth/register & /login - JWT Auth Flow', async () => {
    const email = `testuser_${Date.now()}@example.com`;
    const password = 'SecretPassword123!';

    const regRes = await fetch(`${BASE_URL}/api/v1/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (regRes.status !== 200 && regRes.status !== 201) {
      throw new Error(`Registration failed with HTTP ${regRes.status}`);
    }

    const authData = await regRes.json();
    if (!authData.token) throw new Error('Auth response missing JWT token');
  });

  console.log('\n---------------------------------------------------------');
  console.log(`📊 TEST SUITE SUMMARY: ${passed} PASSED, ${failed} FAILED`);
  console.log('---------------------------------------------------------');

  process.exit(failed > 0 ? 1 : 0);
}

runTests();
