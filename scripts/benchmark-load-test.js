/**
 * Pure Node.js Benchmark & Load Testing Runner
 * Zero external binary required! Measures QPS and p50, p95, p99 Latency percentiles.
 */

const http = require('http');

const BASE_URL = 'http://localhost:8083';
const TOTAL_REQUESTS = 1000;
const CONCURRENCY = 50; 

function requestSeed() {
  return new Promise((resolve) => {
    const postData = JSON.stringify({ longUrl: 'https://github.com/donnemartin/system-design-primer', customAlias: 'sys-primer' });
    const req = http.request(`${BASE_URL}/api/v1/urls`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
      }
    }, (res) => {
      res.on('data', () => {});
      res.on('end', resolve);
    });
    req.on('error', resolve); // ignore errors
    req.write(postData);
    req.end();
  });
}

function makeRequest(url) {
  return new Promise((resolve) => {
    const start = Date.now();
    const req = http.get(url, (res) => {
      res.on('data', () => {});
      res.on('end', () => {
        resolve({ duration: Date.now() - start, status: res.statusCode });
      });
    });
    req.on('error', () => {
      resolve({ duration: Date.now() - start, status: 500 });
    });
  });
}

async function runBenchmark() {
  console.log('---------------------------------------------------------');
  console.log(`🔥 RUNNING HIGH-LOAD SYSTEM BENCHMARK (${TOTAL_REQUESTS} requests, ${CONCURRENCY} concurrent)`);
  console.log('---------------------------------------------------------\n');

  await requestSeed();

  const targetUrl = `${BASE_URL}/sys-primer`;
  const latencies = [];
  let completed = 0;
  let successCount = 0;
  let failCount = 0;
  
  const startTime = Date.now();

  async function worker() {
    while (completed < TOTAL_REQUESTS) {
      completed++;
      const result = await makeRequest(targetUrl);
      latencies.push(result.duration);
      if (result.status === 302 || result.status === 200) {
        successCount++;
      } else {
        failCount++;
      }
    }
  }

  const workers = Array.from({ length: CONCURRENCY }, () => worker());
  await Promise.all(workers);

  const totalTimeSec = (Date.now() - startTime) / 1000;
  const qps = (TOTAL_REQUESTS / totalTimeSec).toFixed(1);

  latencies.sort((a, b) => a - b);
  const min = latencies[0] || 0;
  const max = latencies[latencies.length - 1] || 0;
  const p50 = latencies[Math.floor(latencies.length * 0.50)] || 0;
  const p95 = latencies[Math.floor(latencies.length * 0.95)] || 0;
  const p99 = latencies[Math.floor(latencies.length * 0.99)] || 0;

  console.log(`📊 BENCHMARK RESULTS SUMMARY:`);
  console.log(`  • Total Requests Completed : ${TOTAL_REQUESTS}`);
  console.log(`  • Successful Redirects    : ${successCount}`);
  console.log(`  • Failed Requests         : ${failCount}`);
  console.log(`  • Elapsed Duration        : ${totalTimeSec.toFixed(2)} seconds`);
  console.log(`  • Throughput (QPS)        : ${qps} req/sec`);
  console.log(`  --------------------------------------------------`);
  console.log(`  ⏱️ LATENCY PERCENTILES:`);
  console.log(`    - Min Latency           : ${min} ms`);
  console.log(`    - p50 Latency (Median)  : ${p50} ms`);
  console.log(`    - p95 Latency           : ${p95} ms`);
  console.log(`    - p99 Latency           : ${p99} ms`);
  console.log(`    - Max Latency           : ${max} ms`);
  console.log('---------------------------------------------------------\n');
}

runBenchmark();
