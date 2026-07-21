# System Design & Backend Engineering Top 100 Interview Prep

This document contains senior/staff level interview questions and answers curated specifically for URL Shortener, Distributed Systems, Redis, Kafka, MySQL, and Spring Boot interviews.

---

## Section 1: System Design & Distributed Systems

### Q1: How do you prevent hash collisions in URL shorteners?
**Answer**: By decoupling ID generation from hashing. Instead of hashing long URLs with MD5/SHA256 (which causes collisions and requires DB lookups), generate a 64-bit unique ID using a distributed generator like **Twitter Snowflake** or **DB Auto-Increment**, then encode the ID into **Base62**. Because every generated ID is guaranteed to be unique, Base62 encoding is 100% collision-free.

### Q2: How do you scale a URL shortener to handle 100,000 requests/sec?
**Answer**:
1. **Stateless Web Tier**: Scale Spring Boot nodes horizontally behind a Layer 7 Load Balancer (Nginx/ALB).
2. **Multi-Level Caching**: Place Redis RAM cluster in front of DB. Serve 95%+ requests from Redis cache.
3. **Database Read Replicas**: Distribute read queries across multiple read-only MySQL replicas.
4. **Asynchronous Analytics**: Offload click tracking to Kafka message topics so DB writes do not block the HTTP 302 response thread.

### Q3: What is the difference between Cache-Aside, Write-Through, and Write-Back caching?
**Answer**:
- **Cache-Aside (Used in our project)**: App checks cache. On miss, app reads DB, returns data, and populates cache. Best for read-heavy workloads with unpredictable access patterns.
- **Write-Through**: App writes to cache, and cache synchronously writes to DB. Ensures consistency but adds write latency.
- **Write-Back**: App writes to cache immediately, and cache asynchronously flushes batch writes to DB. High write speed, but risk of data loss if cache crashes before DB write.

### Q4: Why use Apache Kafka instead of a traditional RabbitMQ or Spring `@Async` thread pool for analytics?
**Answer**:
- **Durability & Replayability**: Kafka persists messages on disk in partition logs. If analytics DB goes down for 2 hours, Kafka holds click logs safely and replays them upon recovery.
- **Backpressure Handling**: Kafka consumers pull messages at their own processing rate, preventing database connection starvation under viral spikes.
- **Scalability**: Kafka partitions allow parallel consumption across consumer groups.

---

## Section 2: Java 21, Spring Boot & Security

### Q5: How do Virtual Threads (Java 21 Project Loom) improve high-concurrency web server throughput?
**Answer**: Traditional Java web servers assign 1 OS thread per HTTP request. Operating system threads are expensive (1MB stack memory, kernel context-switch cost). Java 21 Virtual Threads are lightweight user-mode threads managed by the JVM. Millions of virtual threads can run concurrently on top of a few OS carrier threads, eliminating thread starvation during blocking I/O calls (DB/Redis queries).

### Q6: How do you protect a public URL shortener from Open Redirect vulnerabilities?
**Answer**: Attackers use short links to redirect victims to phishing sites (`http://short.ly/xyz` -> `http://malicious-phishing.com`).
**Mitigations**:
1. Domain whitelist / Google Safe Browsing API check before shortening URLs.
2. Display an intermediate warning page for untrusted destination domains.
3. Validate long URL format strictly with URI parsers to prevent header injection (`\r\nLocation:`).

---

## Section 3: Database & Redis

### Q7: Why use Base62 instead of Base64 or Hexadecimal encoding for short URLs?
**Answer**:
- **Hexadecimal (Base16)**: Uses `[0-9][a-f]`. Low character density (7 chars = $16^7 \approx 268$ Million capacity).
- **Base64**: Uses `[0-9][a-z][A-Z]+=/`. Contains reserved URL special characters (`+`, `/`, `=`) which require URL encoding (`%2B`, `%2F`), making short links ugly or broken in chat apps.
- **Base62**: Uses `[0-9][a-z][A-Z]`. Completely URL-safe, alphanumeric, high capacity ($62^7 \approx 3.5$ Trillion capacity).
