# Phase 11: Monitoring, Metrics & Observability Architecture

## 1. Overview

Production system reliability requires real-time insight into application performance, throughput, error rates, and resource utilization.

The application integrates an observability stack featuring:
- **Spring Boot Actuator & Micrometer**: Exposes real-time application metrics at `/actuator/prometheus`.
- **Prometheus** ([prometheus.yml](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/prometheus/prometheus.yml)): Scrapes and stores time-series metric data.
- **Grafana**: Visualizes metrics dashboards and alerts.

---

## 2. Key Metrics Tracked

| Metric Name | Prometheus Metric Identifier | Target SLA / KPI |
| :--- | :--- | :--- |
| **HTTP Redirect Latency (p99)** | `http_server_requests_seconds{uri="/{shortCode}"}` | $< 10 \text{ ms}$ |
| **HTTP Shorten Latency (p99)** | `http_server_requests_seconds{uri="/api/v1/urls/shorten"}` | $< 50 \text{ ms}$ |
| **Cache Hit Ratio** | `redis_keyspace_hits / (hits + misses)` | $> 98\%$ |
| **Kafka Lag** | `kafka_consumergroup_lag{topic="url-click-events"}` | $< 100 \text{ events}$ |
| **Rate Limit Denials** | `http_requests_total{status="429"}` | Alert on spikes |
| **JVM Memory Usage** | `jvm_memory_used_bytes` | $< 80\%$ heap allocation |

---

## 3. Grafana Telemetry Dashboard Architecture

```
+-----------------------------------------------------------------------+
|  URL Shortener Operational Dashboard                                  |
+------------------------------------+----------------------------------+
|  [ Throughput (RPS) ]               |  [ Latency Percentiles (ms) ]    |
|  24.5k requests/sec                |  p50: 1.2ms | p99: 7.8ms         |
+------------------------------------+----------------------------------+
|  [ Cache Hit Rate ]                 |  [ Kafka Consumer Lag ]          |
|  99.4% Hits                        |  0 Messages Lag                  |
+------------------------------------+----------------------------------+
```

---

## 4. Alerting Thresholds
- **High Redirection Latency**: Trigger alert if p99 latency exceeds 50ms for over 2 minutes.
- **Database Connection Pool Exhaustion**: Trigger alert if HikariCP active connections exceed 90% capacity.
- **Service Down**: Trigger immediate PagerDuty alert if backend instance status `/actuator/health` returns `DOWN`.
