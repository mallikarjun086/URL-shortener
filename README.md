# ⚡ SwiftLink Enterprise - Production-Grade Distributed URL Shortener

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache--Kafka-7.5-black.svg)](https://kafka.apache.org/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.txt)

> **SwiftLink Enterprise** is a high-throughput, sub-millisecond distributed URL shortener service engineered to handle extreme concurrency, zero hash collisions, cache-penetration defense, and non-blocking real-time telemetry analytics.

---

## 📌 Table of Contents
- [Architecture Overview](#-architecture-overview)
- [Enterprise Features](#-enterprise-features)
- [Technology Stack](#-technology-stack)
- [Repository Structure](#-repository-structure)
- [Quick Start Guide](#-quick-start-guide)
  - [Option 1: Standalone Development Mode](#option-1-standalone-development-mode)
  - [Option 2: Distributed Stack via Docker Compose](#option-2-distributed-stack-via-docker-compose)
- [Configuration & Environment Variables](#-configuration--environment-variables)
- [REST API Reference & Examples](#-rest-api-reference--examples)
- [Automated Testing & Benchmarks](#-automated-testing--benchmarks)
- [System Design Deep-Dive Documentation](#-system-design-deep-dive-documentation)
- [Contributing & License](#-contributing--license)

---

## 🎯 Architecture Overview

```
                          [ Client Browser ]
                                  │
                                  ▼ (Port 80)
┌───────────────────────────────────────────────────────────────────┐
│                        Nginx Load Balancer                        │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼ (Port 8081 / 8080)
┌───────────────────────────────────────────────────────────────────┐
│                  Spring Boot 3.2 Backend Service                  │
│  ┌───────────────────────────┐     ┌───────────────────────────┐  │
│  │ Base62 + Snowflake ID     │     │ Pure Java Bloom Filter    │  │
│  └───────────────────────────┘     └───────────────────────────┘  │
│  ┌───────────────────────────┐     ┌───────────────────────────┐  │
│  │ Token-Bucket Rate Limiter │     │ JwtAuth Security Filter   │  │
│  └───────────────────────────┘     └───────────────────────────┘  │
└──────┬──────────────────────┬──────────────────────┬──────────────┘
       │                      │                      │
       ▼ (Sub-1ms)            ▼ (O(1) Fallback)     ▼ (Async Event)
┌─────────────┐        ┌──────────────┐       ┌─────────────────────┐
│ Redis Cache │        │ MySQL DB     │       │ Apache Kafka Broker │
└─────────────┘        └──────────────┘       └──────────┬──────────┘
                                                         │
                                                         ▼
                                              ┌─────────────────────┐
                                              │ Analytics Consumer  │
                                              └─────────────────────┘
```

---

## ✨ Enterprise Features

* **⚡ Sub-10ms Redirect Hot-Path**: Combines Java 21 Virtual Threads, Redis Cache-Aside strategy, and a 64-bit Twitter Snowflake ID generator encoded with Base62 for sub-millisecond, zero-collision URL resolution.
* **🛡️ O(1) Cache Penetration Defense**: In-memory Java BitSet Bloom Filter blocks invalid short code requests before hitting MySQL.
* **📊 Event-Driven Telemetry Analytics**: Asynchronous Apache Kafka producer/consumer pipeline tracks click telemetry (Country, Device, OS, Browser, Referrer) without impacting redirect latency.
* **🔒 Stateless Security & Authentication**: BCrypt password hashing, stateless JWT authentication filter chain, and user link history persistence.
* **🚦 Token-Bucket Rate Limiting**: Per-IP rate-limiting filter enforcing 60 requests/minute to defend against DDoS attacks and brute-force abuse.
* **🧹 Scheduled Expired Link Cleaner**: Background `@Scheduled` cleanup service (`ExpiredUrlCleanupService`) to automatically purge expired links from Redis and MySQL.
* **📱 Modern React 18 Dashboard**: Single-page application built with React, Vite, TypeScript, and TailwindCSS featuring instant QR code generation and live analytics visuals.
* **📈 Production Telemetry & Observability**: Metrics collection with Prometheus exporter, Grafana monitoring dashboards, and production Kubernetes deployment manifests (`k8s/`).

---

## 🛠️ Technology Stack

| Component | Technologies Used |
| :--- | :--- |
| **Backend Framework** | Java 21, Spring Boot 3.2, Spring Data JPA, Spring Security |
| **Caching Layer** | Redis 7.0 (Spring Data Redis), Pure Java BitSet Bloom Filter |
| **Messaging & Events** | Apache Kafka 7.5, Zookeeper, Jackson JSON Serializer |
| **Databases** | MySQL 8.0 (Production), H2 In-Memory (Development) |
| **Frontend UI** | React 18, TypeScript, Vite, TailwindCSS, Lucide Icons |
| **DevOps & Observability** | Docker Compose, Nginx, Prometheus, Grafana, Kubernetes (`k8s/`) |

---

## 📂 Repository Structure

```
URL-Shortener/
├── backend/                  # Spring Boot 3.2 Java 21 Backend Application
│   ├── src/main/java/        # Controllers, Services, Repositories, Security, Entities
│   ├── src/test/java/        # Unit & Integration Test Suites
│   ├── Dockerfile            # Container definition for backend
│   └── pom.xml               # Maven dependencies and build setup
├── frontend/                 # React 18 + TypeScript Dashboard
│   ├── src/                  # React components, pages, context, and styles
│   ├── package.json          # Frontend dependencies
│   └── vite.config.ts        # Vite bundle configuration
├── docs/                     # System Design & Phase Architecture Documentation
│   ├── architecture-decision-records/  # Architectural Decision Records (ADRs)
│   ├── cheat-sheets/         # Quick reference technical cheat sheets
│   └── phase-*.md            # Detailed phase documentation (00 through 12)
├── k8s/                      # Kubernetes manifests (Deployment, Service, ConfigMap)
├── nginx/                    # Nginx Reverse Proxy & Load Balancer configuration
├── prometheus/               # Prometheus metrics collection rules
├── scripts/                  # Load testing and API validation scripts (k6, PowerShell)
├── docker-compose.yml        # Multi-container orchestration specification
├── CONTRIBUTING.md           # Contribution guidelines
├── LICENSE.txt               # MIT Open Source License
└── README.md                 # Project README
```

---

## 🚀 Quick Start Guide

### Option 1: Standalone Development Mode

For rapid local testing without external infrastructure, run using the in-memory H2 database configuration:

1. **Launch Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   - **Backend API URL**: `http://localhost:8081`
   - **Swagger UI API Docs**: `http://localhost:8081/swagger-ui.html`
   - **H2 In-Memory Console**: `http://localhost:8081/h2-console` *(JDBC URL: `jdbc:h2:mem:urlshortener`, User: `sa`, Pass: empty)*

2. **Launch Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   - **Frontend Application**: `http://localhost:5173`

---

### Option 2: Distributed Stack via Docker Compose

Launch the full distributed architecture including Nginx gateway, MySQL 8, Redis 7, Kafka, Zookeeper, Prometheus, and Grafana:

```bash
docker-compose up --build
```

| Service Gateway | Endpoint URL | Credentials |
| :--- | :--- | :--- |
| **Nginx Gateway** | `http://localhost:80` | N/A |
| **Backend REST Service** | `http://localhost:8080` | N/A |
| **Prometheus Exporter** | `http://localhost:9090` | N/A |
| **Grafana Dashboards** | `http://localhost:3000` | User: `admin` / Pass: `admin` |

---

## ⚙️ Configuration & Environment Variables

| Variable Name | Default Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev` or `prod`) |
| `APP_DOMAIN` | `http://localhost:8081` | Base domain for generated short URLs |
| `WORKER_ID` | `1` | Snowflake ID generator worker node ID (0-31) |
| `DATACENTER_ID` | `1` | Snowflake ID generator datacenter ID (0-31) |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/url_shortener` | MySQL connection string |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `secret` | Database password |
| `SPRING_REDIS_HOST` | `localhost` | Redis server hostname |
| `SPRING_REDIS_PORT` | `6379` | Redis server port |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker endpoints |

---

## 📡 REST API Reference & Examples

### Endpoints Overview

| HTTP Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user account | No |
| `POST` | `/api/v1/auth/login` | Authenticate user & return JWT token | No |
| `POST` | `/api/v1/urls` | Create a short URL (supports TTL expiration & custom alias) | Optional |
| `GET` | `/{shortCode}` | Resolve short code and execute HTTP 302 redirect | No |
| `GET` | `/api/v1/urls/my-urls` | Retrieve all short URLs created by authenticated user | Yes (JWT) |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Retrieve real-time click statistics and telemetry | No |

---

### API Usage Examples

#### 1. Shorten a Long URL
```bash
curl -X POST http://localhost:8081/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com/long-page-path",
    "customAlias": "my-custom-link",
    "expiresAt": "2026-12-31T23:59:59Z"
  }'
```

**Sample Response**:
```json
{
  "shortCode": "my-custom-link",
  "shortUrl": "http://localhost:8081/my-custom-link",
  "originalUrl": "https://example.com/long-page-path",
  "createdAt": "2026-08-03T10:00:00Z",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

#### 2. Resolve & Redirect Short Code
```bash
curl -i http://localhost:8081/my-custom-link
```

**Sample Response**:
```http
HTTP/1.1 302 Found
Location: https://example.com/long-page-path
```

---

## 🧪 Automated Testing & Benchmarks

Run the complete backend test suite (Snowflake ID generator, Base62 encoder, JWT authentication, Controllers, and Cleanup Services):

```bash
cd backend
mvn clean test
```

Execute automated API validation scripts:
```powershell
# PowerShell API Test Runner
./scripts/test-api.ps1
```

Run k6 performance and load testing:
```bash
# Node / k6 Load Testing Script
node scripts/benchmark-load-test.js
```

---

## 📚 System Design Deep-Dive Documentation

Complete step-by-step architectural design documentation is available in the [`docs/`](docs/) directory:

- 📘 [Phase 00: Software Engineering Mindset & Principles](docs/phase-00-software-engineering-mindset.md)
- 📘 [Phase 01: Requirements & Capacity Planning](docs/phase-01-requirements-and-capacity-planning.md)
- 📘 [Phase 02: High-Level System Architecture Design](docs/phase-02-high-level-design.md)
- 📘 [Phase 03: Database Schema & Indexing Strategy](docs/phase-03-database-design.md)
- 📘 [Phase 04: RESTful API Specifications](docs/phase-04-api-design.md)
- 📘 [Phase 05: Snowflake ID Generator & Base62 Encoding](docs/phase-05-deep-dive-snowflake-base62.md)
- 📘 [Phase 06: Caching & Bloom Filter Defense Strategy](docs/phase-06-caching-bloom-filter-strategy.md)
- 📘 [Phase 07: Asynchronous Event-Driven Telemetry Analytics](docs/phase-07-async-event-driven-analytics.md)
- 📘 [Phase 08: Stateless Security & Token-Bucket Rate Limiting](docs/phase-08-security-rate-limiting.md)
- 📘 [Phase 09: Scalability, High Availability & Load Balancing](docs/phase-09-scalability-ha-load-balancing.md)
- 📘 [Phase 10: Containerization & Kubernetes Deployment](docs/phase-10-containerization-k8s.md)
- 📘 [Phase 11: Observability, Metrics & Telemetry Stack](docs/phase-11-monitoring-observability.md)
- 📘 [Phase 12: Comprehensive Testing & Load Benchmarks](docs/phase-12-testing-strategy.md)
- 🎓 [Senior System Design Interview Top 100 Prep](docs/interview-preparation-top-100.md)
- 📖 [Glossary & Technical Terminology](docs/glossary.md)
- 📑 [Architecture Decision Records (ADRs)](docs/architecture-decision-records/ADR-000-foundational-architecture.md)

---

## 📜 Contributing & License

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on code standards and pull request workflows.

This project is licensed under the MIT License - see [LICENSE.txt](LICENSE.txt) for details.
