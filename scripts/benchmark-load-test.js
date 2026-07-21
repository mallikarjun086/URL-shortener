/**
 * Pure Node.js Benchmark & Load Testing Runner
 * Zero external binary required! Measures QPS and p50, p95, p99 Latency percentiles.
 */

const BASE_URL = 'http://localhost:8081';
const TOTAL_REQUESTS = 1000;
const CONCURRENCY = 50; // 50 concurrent virtual connections

async function runBenchmark() {
  console.log('---------------------------------------------------------');
  console.log(`🔥 RUNNING HIGH-LOAD SYSTEM BENCHMARK (${TOTAL_REQUESTS} requests, ${CONCURRENCY} concurrent)`);
  console.log('---------------------------------------------------------\n');

  const targetUrl = `${BASE_URL}/sys-primer`;
  const latencies = [];
  let completed = 0;
  let successCount = 0;
  let failCount = 0;

  const startTime = Date.now();

  async function worker() {
    while (completed < TOTAL_REQUESTS) {
      completed++;
      const reqStart = Date.now();
      try {
        const res = await fetch(targetUrl, { redirect: 'manual' });
        const reqDuration = Date.now() - reqStart;
        latencies.push(reqDuration);
        if (res.status === 302 || res.status === 200) {
          successCount++;
        } else {
          failCount++;
        }
      } catch (err) {
        failCount++;
      }
    }
  }

  // Launch concurrent workers
  const workers = Array.from({ length: CONCURRENCY }, () => worker());
  await Promise.all(workers);

  const totalTimeSec = (Date.now() - startTime) / 1000;
  const qps = (TOTAL_REQUESTS / totalTimeSec).toFixed(1);

  // Sort latencies to compute percentiles
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
