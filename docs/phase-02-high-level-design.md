# Phase 2: High Level System Architecture & Data Flow

```
+-------------------------------------------------------------------------------------------------------+
|                                  SYSTEM HIGH LEVEL ARCHITECTURE                                       |
+-------------------------------------------------------------------------------------------------------+

                                    [ Client Application / User ]
                                                  |
                                                  v
                                     [ DNS / Cloudflare CDN ]
                                                  |
                                                  v
                              [ Nginx Reverse Proxy / Load Balancer ]
                                     /                    \
                                    /                      \
                                   v                        v
                    [ Spring Boot API Nodes ]       [ Spring Boot API Nodes ]
                           (Node 1)                        (Node 2)
                           /   |   \                      /   |   \
                          /    |    \                    /    |    \
                         v     v     v                  v     v     v
              +---------+  +----+  +-----+  +----------+  +----+  +-----+
              | Redis   |  |MySQL| |Kafka|  | Redis    |  |MySQL| |Kafka|
              | Cluster |  | Master| Cluster| Cluster  |  |Replica| Cluster
              +---------+  +----+  +-----+  +----------+  +----+  +-----+
                                      |
                                      v
                          [ Kafka Analytics Consumer ]
                                      |
                                      v
                          [ MySQL Analytics DB ]
```

---

## Data Flow Scenarios

### 1. Write Flow (URL Shortening Request)
1. Client issues `POST /api/v1/urls` with `{ "longUrl": "https://example.com/item" }` and JWT Bearer token.
2. Nginx routes request to a Spring Boot App Node.
3. App Node authenticates JWT token and checks Bucket4j / Redis Rate Limiter.
4. App Node generates 64-bit unique ID using **Twitter Snowflake Algorithm**.
5. Base62 encoder converts 64-bit ID to 7-character short string (e.g. `8xK2qP`).
6. App Node persists `(shortCode, longUrl, userId, createdAt)` in MySQL Master DB.
7. App Node pre-populates Redis RAM cache (`shortCode` -> `longUrl`).
8. App Node returns `201 Created` with shortened URL.

### 2. Read Flow (Redirect Request - Sub-10ms Target)
1. Client hits `GET /8xK2qP`.
2. Nginx forwards to Spring Boot App Node.
3. App Node performs Redis Cache lookup:
   - **Cache Hit (95% of requests)**: Returns `longUrl` directly in ~1ms.
   - **Cache Miss**: Queries MySQL Read Replica, stores `longUrl` in Redis with 24h TTL, and returns `longUrl`.
4. App Node emits an asynchronous `ClickEvent` message to **Apache Kafka topic `url-clicks`**.
5. App Node immediately returns `302 Found` with `Location: longUrl`.

### 3. Asynchronous Analytics Flow
1. **Kafka Analytics Consumer** reads batches of click events from topic `url-clicks`.
2. Consumer parses IP geo-location, user-agent device/browser/OS parameters.
3. Consumer performs batch upsert into MySQL `click_analytics` table every 5 seconds.
4. **Benefit**: Zero impact on user redirect latency!
