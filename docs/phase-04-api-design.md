# Phase 4: REST API Specifications (OpenAPI Standard)

## Endpoints Summary

| HTTP Verb | Path | Description | Authentication | Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user | Public | `201 Created`, `400 Bad Request` |
| `POST` | `/api/v1/auth/login` | Login and obtain JWT token | Public | `200 OK`, `401 Unauthorized` |
| `POST` | `/api/v1/urls` | Create shortened URL | Bearer JWT | `201 Created`, `400 Bad Request`, `429 Rate Limit` |
| `GET` | `/{shortCode}` | Resolve short code & redirect | Public | `302 Found`, `404 Not Found` |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Get analytics metrics for link | Bearer JWT | `200 OK`, `403 Forbidden`, `404 Not Found` |
| `DELETE` | `/api/v1/urls/{shortCode}` | Delete a shortened link | Bearer JWT | `204 No Content`, `403 Forbidden` |

---

## Sample Request / Response Contracts

### 1. Create Short URL (`POST /api/v1/urls`)
#### Request:
```json
{
  "longUrl": "https://donnemartin.com/system-design-primer?ref=portfolio",
  "customAlias": "sys-primer",
  "expiresInDays": 30
}
```

#### Response (`201 Created`):
```json
{
  "shortCode": "sys-primer",
  "shortUrl": "http://short.ly/sys-primer",
  "originalUrl": "https://donnemartin.com/system-design-primer?ref=portfolio",
  "createdAt": "2026-07-19T12:54:00Z",
  "expiresAt": "2026-08-18T12:54:00Z",
  "clickCount": 0
}
```

### 2. Get Link Analytics (`GET /api/v1/urls/sys-primer/analytics`)
#### Response (`200 OK`):
```json
{
  "shortCode": "sys-primer",
  "totalClicks": 1450,
  "topCountries": {
    "United States": 650,
    "India": 400,
    "Germany": 200,
    "Other": 200
  },
  "deviceBreakdown": {
    "Desktop": 900,
    "Mobile": 500,
    "Tablet": 50
  },
  "topBrowsers": {
    "Chrome": 1000,
    "Safari": 300,
    "Firefox": 150
  }
}
```
