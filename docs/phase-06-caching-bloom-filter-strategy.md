# Phase 06: Caching & Bloom Filter Strategy

## 1. Overview

In a URL Shortener system, read operations (resolving short URLs to long URLs) outnumber write operations (creating short URLs) by a factor of 100:1 to 1000:1.

To deliver sub-10ms redirection latencies and protect primary database resources, this application implements a **multi-tier caching architecture**:
1. **Guava / Redis Bloom Filter** ([BloomFilterService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/BloomFilterService.java)): Probabilistic gatekeeper to prevent Cache Penetration.
2. **Redis In-Memory Key-Value Store** ([RedisCacheService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/RedisCacheService.java)): High-performance cache for hot URL mappings.

---

## 2. Multi-Layer Request Resolution Flow

```
Client Request /{shortCode}
        │
        v
+-------------------+      No (100% Definite)
|   Bloom Filter    | ------------------------> Return HTTP 404 (0 DB/Cache Hit)
+---------+---------+
          | Yes (99.9% Probable)
          v
+-------------------+      Hit (<1ms)
|    Redis Cache    | ------------------------> Return Original URL (HTTP 302)
+---------+---------+
          | Miss
          v
+-------------------+
|  MySQL Database   | ------------------------> Populate Redis & Return URL (HTTP 302)
+-------------------+
```

---

## 3. Defense Against Common Cache Vulnerabilities

### A. Cache Penetration Defense (Bloom Filter)
- **Problem**: Malicious bots flood the system with millions of non-existent short keys (`/fake123`, `/attack456`). If keys don't exist in Redis, queries pass directly to MySQL, causing database CPU spikes and service outages.
- **Solution**: The Bloom Filter records all valid `shortCode` entries using multiple hash functions over a bit array.
  - If `bloomFilter.mightContain(shortCode) == false`, the key **definitely does not exist**. The request is rejected in $O(1)$ constant time without touching Redis or MySQL.

### B. Cache Breakdown Defense (Pre-Warming)
- **Problem**: When a new URL is generated, its first click would hit the database (cache miss).
- **Solution**: Upon link creation in [UrlShortenerService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/UrlShortenerService.java#L85-L87), the mapping is pre-warmed directly into Redis (`redisCacheService.cacheUrl(shortCode, longUrl)`), guaranteeing sub-1ms resolution on the very first hit.

### C. Cache Eviction & Expiration Policy
- **TTL (Time to Live)**: Configured with Redis TTL matching URL expiration dates or a standard 30-day Least Recently Used (LRU) eviction policy.
