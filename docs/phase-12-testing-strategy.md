# Phase 12: Testing Strategy & Verification Guide

Testing a distributed URL shortener requires verifying unit correctness, cache consistency, asynchronous messaging reliability, and high-concurrency throughput.

---

## 1. Unit Testing Strategy

Unit tests isolate components from external infrastructure (No real DB or Redis required).

- **Base62 Encoding Unit Test**: [Base62EncoderTest.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/test/java/com/urlshortener/algorithm/Base62EncoderTest.java)
  - Tests mathematical bidirectional encoding (`encode(id) -> str -> decode(str) -> id`).
- **Snowflake ID Generator Unit Test**: [SnowflakeIdGeneratorTest.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/test/java/com/urlshortener/algorithm/SnowflakeIdGeneratorTest.java)
  - Tests generation of 10,000 IDs in a single thread to guarantee uniqueness and monotonic ordering.
- **Service Layer Mock Test**: [UrlShortenerServiceTest.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/test/java/com/urlshortener/service/UrlShortenerServiceTest.java)
  - Uses Mockito to verify Redis caching and Kafka click event publishing.

To run unit tests via command line:
```bash
cd backend
mvn clean test
```

---

## 2. API Integration Testing (cURL / Postman)

### Step 1: Health & Metrics Endpoint Check
```bash
curl http://localhost:8080/actuator/health
```
**Expected Response**: `{"status":"UP"}`

---

### Step 2: Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"engineer@example.com","password":"password123"}'
```
**Expected Response** (`201 Created`):
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "engineer@example.com",
  "role": "ROLE_USER"
}
```

---

### Step 3: Shorten a Long URL
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{"longUrl":"https://donnemartin.com/system-design-primer","customAlias":"sys-primer"}'
```
**Expected Response** (`201 Created`):
```json
{
  "shortCode": "sys-primer",
  "shortUrl": "http://localhost:8080/sys-primer",
  "originalUrl": "https://donnemartin.com/system-design-primer",
  "clickCount": 0
}
```

---

### Step 4: Test Sub-10ms Redirection (HTTP 302)
```bash
curl -i http://localhost:8080/sys-primer
```
**Expected Response**:
```http
HTTP/1.1 302 Found
Location: https://donnemartin.com/system-design-primer
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

---

### Step 5: Verify Analytics Aggregation
```bash
curl -X GET http://localhost:8080/api/v1/urls/sys-primer/analytics \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

## 3. Automated PowerShell Verification Script

Run the pre-configured Windows script:
```powershell
.\scripts\test-api.ps1
```

---

## 4. Load & Performance Testing (k6 / Apache Bench)

To test sub-10ms redirect latency under 1,000 concurrent virtual users using Apache Bench (`ab`):

```bash
ab -n 50000 -c 100 http://localhost:8080/sys-primer
```

Key Metrics to observe:
- **Requests per second (RPS)**: Should exceed `5,000 RPS`.
- **Time per request (mean)**: Should be $<5\text{ ms}$.
