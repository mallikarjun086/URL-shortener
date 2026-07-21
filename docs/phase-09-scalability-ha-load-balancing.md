# Phase 09: Scalability, High Availability & Load Balancing

## 1. Overview

To guarantee 99.99% system uptime and handle tens of thousands of concurrent redirect requests, the system is designed to scale horizontally across every tier.

---

## 2. Nginx Load Balancing Configuration

An **Nginx Reverse Proxy** ([nginx.conf](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/nginx/nginx.conf)) acts as the single ingress entry point, distributing incoming HTTP traffic across stateless backend instances using weighted round-robin load balancing.

```nginx
upstream url_shortener_backend {
    least_conn;
    server backend-node-1:8080 max_fails=3 fail_timeout=10s;
    server backend-node-2:8080 max_fails=3 fail_timeout=10s;
    server backend-node-3:8080 max_fails=3 fail_timeout=10s;
}
```

---

## 3. High Availability (HA) Across Layers

```
                       [ Incoming Web Traffic ]
                                  │
                                  v
                      +------------------------+
                      | Nginx Load Balancers   | (Active-Passive VRRP)
                      +-----------+------------+
                                  │
          ┌───────────────────────┼───────────────────────┐
          v                       v                       v
   +--------------+        +--------------+        +--------------+
   | Backend App  |        | Backend App  |        | Backend App  | (Stateless Spring Boot)
   +------+-------+        +------+-------+        +------+-------+
          │                       │                       │
          └───────────────────────┼───────────────────────┘
                                  │
                ┌─────────────────┴─────────────────┐
                v                                   v
     +---------------------+             +---------------------+
     | Redis Sentinel/Cluster|           | MySQL Master/Replica| (Read Replicas)
     +---------------------+             +---------------------+
```

### Key HA Strategies:
- **Stateless Backend Tier**: Application servers store no local session state. Any backend instance can process any shorten or redirect request.
- **MySQL Read Replicas**: Write queries (short URL creation) hit the Primary MySQL master node, while read queries (resolving short URLs on cache miss) scale out across Read Replicas.
- **Redis Sentinel / Cluster**: Master-Replica Redis setup with automatic failover in <5 seconds.
