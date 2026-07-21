# ⚡ SwiftLink Enterprise - Production-Grade Distributed URL Shortener Service

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache--Kafka-7.5-black.svg)](https://kafka.apache.org/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.txt)

> **SwiftLink Enterprise** is a high-throughput, low-latency distributed URL shortener engineered to solve high-concurrency redirecting, cache-penetration defense, and non-blocking telemetry analytics at hyper-scale.

---

## 🎯 Architecture Overview

```
[ Client Browser ] 
        │
        ▼ (Port 80)
┌───────────────────────────────────────────────────────────┐
│                    Nginx Load Balancer                    │
└─────────────────────────────┬─────────────────────────────┘
                              │
                              ▼ (Port 8081 / 8080)
┌───────────────────────────────────────────────────────────┐
│              Spring Boot 3.2 Backend Service              │
│  ┌───────────────────────┐   ┌─────────────────────────┐  │
│  │ Base62 Snowflake ID   │   │ Pure Java Bloom Filter  │  │
│  └───────────────────────┘   └─────────────────────────┘  │
│  ┌───────────────────────┐   ┌─────────────────────────┐  │
│  │ RateLimiter Filter    │   │ JwtAuth Security Filter │  │
│  └───────────────────────┘   └─────────────────────────┘  │
└──────┬──────────────────┬──────────────────┬──────────────┘
       │                  │                  │
       ▼ (Sub-1ms)        ▼ (O(1))           ▼ (Async Event)
┌─────────────┐    ┌──────────────┐   ┌─────────────────────┐
│ Redis Cache │    │ MySQL DB     │   │ Apache Kafka Broker │
└─────────────┘    └──────────────┘   └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │ Analytics Consumer  │
                                      └─────────────────────┘
```

---

## ✨ Enterprise Features

* **⚡ Sub-10ms Redirect Hot-Path**: Combines Java 21 Virtual Threads, Redis Cache-Aside, and a 64-bit Twitter Snowflake ID generator encoded with Base62 for sub-millisecond URL resolution.
* **🛡️ O(1) Cache Penetration Defense**: Built-in pure Java BitSet Bloom Filter to defend against malicious cache-penetration DB lookup attacks.
* **📊 Asynchronous Event-Driven Analytics**: Apache Kafka event producer/consumer pipeline to track real-time click telemetry (Country, Device, OS, Browser, Referrer) without blocking the redirect path.
* **🔒 Stateless JWT Security & Auth**: Password hashing with BCrypt, stateless JWT validation filter chain (`JwtAuthenticationFilter`), and user link history persistence (`GET /api/v1/urls/my-urls`).
* **🚦 Token-Bucket Rate Limiting**: Per-IP rate-limiting filter enforcing 60 requests/minute to protect APIs against DDoS abuse.
* **🧹 Scheduled Expired Link Cleaner**: Background `@Scheduled` cleanup service (`ExpiredUrlCleanupService`) to purge expired links from Redis and MySQL.
* **📱 Instant QR Code & Modern UI**: Responsive single-page React + TailwindCSS dashboard with instant mobile QR code generation and copy-to-clipboard actions.
* **📈 Full Observability Stack**: Production telemetry setup with Prometheus metrics exporter, Grafana dashboards, and Kubernetes manifests (`k8s/`).

---

## 🛠️ Technology Stack

| Layer | Technologies Used |
| :--- | :--- |
| **Backend Core** | Java 21, Spring Boot 3.2, Spring Data JPA, Spring Security |
| **Caching & In-Memory** | Redis (Spring Data Redis), BitSet Bloom Filter |
| **Messaging & Events** | Apache Kafka, Zookeeper, Jackson JSON |
| **Databases** | MySQL 8.0 (Prod), H2 In-Memory (Dev) |
| **Frontend** | React 18, TypeScript, Vite, TailwindCSS, Lucide Icons |
| **DevOps & Infra** | Docker Compose, Nginx, Prometheus, Grafana, Kubernetes |

---

## 🚀 Quick Start Guide

### Option 1: Run Locally (Standalone Dev Mode)

The application includes an in-memory H2 configuration for quick local development without needing external services.

1. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   * *Backend API Base*: `http://localhost:8081`
   * *Swagger API Docs*: `http://localhost:8081/swagger-ui.html`
   * *H2 Console*: `http://localhost:8081/h2-console`

2. **Start Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   * *Frontend Dashboard*: `http://localhost:5173`

---

### Option 2: Run Distributed Stack with Docker Compose

To launch MySQL, Redis, Kafka, Zookeeper, Nginx, Prometheus, and Grafana:

```bash
docker-compose up --build
```

| Service | Endpoint URL |
| :--- | :--- |
| **Application Gateway (Nginx)** | `http://localhost:80` |
| **Backend Service** | `http://localhost:8080` |
| **Prometheus Metrics** | `http://localhost:9090` |
| **Grafana Dashboards** | `http://localhost:3000` *(User: admin / Pass: admin)* |

---

## 📡 REST API Documentation

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register new user account | No |
| `POST` | `/api/v1/auth/login` | Login & receive JWT Bearer token | No |
| `POST` | `/api/v1/urls` | Shorten a URL (Guest or User) | Optional |
| `GET` | `/{shortCode}` | Resolve short code and redirect | No |
| `GET` | `/api/v1/urls/my-urls` | Get all URLs created by current user | Yes (JWT) |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Get click analytics breakdown | No |

---

## 🧪 Automated Testing

Run the automated backend test suite (unit tests for Base62, Snowflake ID, JwtTokenProvider, Controllers, and Services):

```bash
cd backend
mvn clean test
```

---

## 📚 System Design Deep-Dive Documentation

Detailed architectural phase documentation is available in the [`docs/`](docs/) directory:
- [Phase 01: Requirements & Capacity Planning](docs/phase-01-requirements-and-capacity-planning.md)
- [Phase 02: High-Level Design](docs/phase-02-high-level-design.md)
- [Phase 03: Database Design](docs/phase-03-database-design.md)
- [Phase 05: Snowflake ID & Base62 Encoding](docs/phase-05-deep-dive-snowflake-base62.md)
- [Phase 06: Caching & Bloom Filter Strategy](docs/phase-06-caching-bloom-filter-strategy.md)
- [Phase 07: Async Event-Driven Analytics](docs/phase-07-async-event-driven-analytics.md)
- [Phase 08: Security & Rate Limiting](docs/phase-08-security-rate-limiting.md)

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
