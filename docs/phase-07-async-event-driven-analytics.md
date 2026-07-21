# Phase 07: Asynchronous Event-Driven Click Analytics Pipeline

## 1. Overview

Recording click metrics (IP address, user-agent, geolocation, timestamp, referrer) directly during a URL redirect request introduces significant HTTP latency (50-200ms) and database write contention.

To solve this, the application decouples click redirection from metrics tracking using an **Apache Kafka event-driven streaming pipeline**:
- **Kafka Producer** ([KafkaAnalyticsProducerService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/KafkaAnalyticsProducerService.java)): Publishes click events asynchronously.
- **Kafka Consumer** ([KafkaAnalyticsConsumerService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/KafkaAnalyticsConsumerService.java)): Consumes and processes click streams in background thread pools.

---

## 2. Event-Driven Telemetry Architecture

```
User Click HTTP GET /{shortCode}
       │
       ├─────────────────────────────────────────┐
       │                                         │
       v (Sync - <10ms)                          v (Async - Non-blocking)
Return HTTP 302 Found                    Publish Event to Kafka Topic
                                           "url-click-events"
                                                 │
                                                 v
                                         Kafka Consumer Group
                                                 │
                                                 ├── Resolve GeoIP Country
                                                 ├── Parse User-Agent (OS, Browser)
                                                 v
                                         Persist ClickAnalytics Entity
```

---

## 3. Kafka Event Payload & Consumer Processing

### Event Schema:
```json
{
  "urlId": 1748293849182394,
  "ipAddress": "192.168.1.50",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
  "referrer": "https://t.co/",
  "timestamp": 1721562400000
}
```

### Telemetry Enrichment:
1. **IP Geolocation**: Resolved via [GeoIpResolverService.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/service/GeoIpResolverService.java).
2. **User-Agent Parsing**: Extracted into Device Type (`Mobile`/`Desktop`), Operating System (`Windows`, `macOS`, `Android`, `iOS`), and Browser (`Chrome`, `Safari`, `Firefox`, `Edge`).
3. **Atomic Counter Updates**: Updates total click counts on `UrlMapping` entities using atomic database queries (`incrementClickCount`).
