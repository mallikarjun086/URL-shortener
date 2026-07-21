# Phase 0 Cheat Sheet: System Fundamentals

## 1. Network & Protocol Quick Reference

```
+-----------------------------------------------------------------------+
| Protocol Layer | Data Unit   | Examples       | URL Shortener Role    |
+----------------+-------------+----------------+-----------------------+
| Application    | Data/Message| HTTP, TLS, REST| URL short codes, API  |
| Transport      | Segment     | TCP, UDP       | Reliable byte stream  |
| Network        | Packet      | IP (IPv4/IPv6) | Routing across nodes  |
| Data Link      | Frame       | Ethernet, WiFi | Local network transfer|
+----------------+-------------+----------------+-----------------------+
```

## 2. HTTP Status Code Cheat Sheet for URL Shortener

- `301 Moved Permanently`: Browser caches redirect aggressively. Reduces server load, but backend loses analytics/click-tracking on subsequent visits.
- `302 Found`: Temporary redirect. Browser checks server on every request. **Required for accurate click analytics & geographic tracking.**
- `307 Temporary Redirect`: Same as 302, but guarantees the HTTP method (GET/POST) remains unchanged upon redirect.
- `400 Bad Request`: Invalid long URL format or missing required payload.
- `404 Not Found`: Short URL code does not exist or has expired.
- `429 Too Many Requests`: Client exceeded rate limit quotas.

## 3. High Availability Math
- 99% ("Two Nines") = 3.65 days downtime / year
- 99.9% ("Three Nines") = 8.76 hours downtime / year
- 99.99% ("Four Nines") = 52.6 minutes downtime / year
- 99.999% ("Five Nines") = 5.26 minutes downtime / year

## 4. Architectural Rules of Thumb
- **Rule 1**: Always put Redis RAM cache in front of MySQL for high-throughput read workloads.
- **Rule 2**: Prefer horizontal scaling ($AP$) for stateless web servers and read replicas.
- **Rule 3**: Never rely on a single load balancer or single database instance (eliminate SPOFs).
