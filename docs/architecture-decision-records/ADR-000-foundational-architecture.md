# ADR-000: Foundational System Architecture & Trade-Off Matrix

- **Status**: Accepted
- **Deciders**: Staff Software Engineer & System Architect
- **Date**: 2026-07-19

---

## 1. Context and Problem Statement

When designing a production-ready, globally distributed URL shortener system, we must establish foundational architectural standards around networking, serialization, caching, data consistency, and communication protocols before implementing application services.

---

## 2. Decision Drivers

- **Sub-10ms Latency Requirement**: Short URL redirects (`GET /{shortCode}`) account for >90% of system traffic.
- **High Read-to-Write Ratio**: Expected ~100:1 read-to-write ratio (100 million reads/day vs 1 million writes/day).
- **High Availability (99.999%)**: Short URLs are embedded in marketing campaigns, SMS, and global ads; outages break downstream business conversions.
- **Security & Integrity**: Prevention of Open Redirect exploits, DDoS attacks, and unauthorized link modifications.

---

## 3. Considered Options

### Architectural Paradigm Options
1. **REST over HTTP/2 with JSON (Chosen for Public API)**: Standardized, universally compatible with browsers and third-party API clients.
2. **gRPC over HTTP/2 with Protobuf (Chosen for Internal Services)**: Microservice-to-microservice high-throughput communication.
3. **GraphQL**: Flexible client querying. Rejected for redirect path due to parsing overhead and cache invalidation complexity.

### Consistency Model Options (CAP Theorem)
1. **Strict Consistency ($CP$)**: Synchronous multi-region database writes. Rejected for core redirects due to cross-region write latency ($>200\text{ ms}$).
2. **Eventual Consistency ($AP$) (Chosen)**: Asynchronous replication across database read replicas and distributed Redis caches. Redirects function even if secondary read replicas lag by a few milliseconds.

---

## 4. Architectural Decision Summary Matrix

| Decision Area | Selected Strategy | Rejected Alternatives | Primary Rationale |
| :--- | :--- | :--- | :--- |
| **API Protocol** | RESTful HTTP/2 + JSON | SOAP, GraphQL | Universal browser support for `302/301` HTTP redirects. |
| **Network Security** | HTTPS (TLS 1.3) | HTTP Plain | Mandatory data encryption and payload tamper prevention. |
| **Read Topology** | Cache-Aside (Redis RAM) | Direct Database Read | Reduces DB read load by 95%+ and delivers ~1ms latency. |
| **Consistency** | Eventual Consistency ($AP$) | Strong Consistency ($CP$) | Prevents redirect failures during regional network partition. |
| **Load Balancing** | L4 (TCP) + L7 (Nginx Proxy) | DNS Round Robin alone | Allows SSL termination, health-check failover, and path routing. |

---

## 5. Consequences & Mitigations

### Positive Consequences
- Exceptional read throughput (100,000+ requests/sec with minimal nodes).
- Sub-5ms average redirect response time when served from Redis cache.
- System resiliency against regional node outages.

### Negative Consequences / Risks
- **Cache Invalidation Delay**: If a user updates a link target, cached copies in Redis or Edge CDN may serve stale destinations until TTL expires.
- **Mitigation**: Implement explicit Redis key eviction on link modification/deletion events via Kafka message broadcast.
