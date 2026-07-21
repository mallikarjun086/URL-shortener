# Phase 08: Security & Token Bucket Rate Limiting

## 1. Overview

High-scale public APIs are frequent targets for DDoS attacks, brute-force link scraping, and resource starvation.

This application protects system resources using a two-pillar security architecture:
1. **Spring Security & Stateless JWT Authentication** ([SecurityConfig.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/security/SecurityConfig.java), [JwtTokenProvider.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/security/JwtTokenProvider.java)).
2. **Token Bucket API Rate Limiting** ([RateLimiterFilter.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/filter/RateLimiterFilter.java)).

---

## 2. Token Bucket Rate Limiter Algorithm

```
                 Incoming Request (Client IP)
                              │
                              v
                 +--------------------------+
                 |  Get Token Bucket for IP |
                 +------------+-------------+
                              │
                    Has Available Tokens?
                   /                     \
                Yes                       No
                /                           \
  Consume 1 Token                       Return HTTP 429
Pass to Next Filter                   "Too Many Requests"
```

### Technical Specification:
- **Bucket Capacity**: 100 requests per IP address.
- **Refill Window**: Refills tokens every 60 seconds.
- **Response Headers**:
  - `X-RateLimit-Limit`: Maximum tokens (100).
  - `X-RateLimit-Remaining`: Current available token count.

---

## 3. JWT Stateless Authentication Flow

1. User authenticates via `/api/v1/auth/login` or `/api/v1/auth/register` ([AuthController.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/controller/AuthController.java)).
2. Backend signs a cryptographically secure JWT token containing `userEmail` and expiration timestamp.
3. Protected endpoints (`/api/v1/urls/shorten`, `/api/v1/analytics/**`) validate the JWT token in `Bearer` header.
