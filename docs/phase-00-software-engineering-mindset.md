# Phase 0: Software Engineering Mindset & System Fundamentals

Welcome to the engineering journey of building a **Production-Grade, Distributed URL Shortener**. Before writing a single line of Java or React code, a Staff Engineer must understand the physics of distributed systems, networking fundamentals, data serialization, system trade-offs, and failure modes.

---

## 1. Core Concepts & Architecture Deep-Dive

### 1.1 Client-Server Model & Network Flow

In modern web applications, the **Client** (Browser, Mobile App, CLI, or microservice) and **Server** (Spring Boot application, API Gateway, Nginx) operate over standard protocols to request and deliver resources.

```
+-----------------------------------------------------------------------------------+
|                                 CLIENT-SERVER FLOW                                |
+-----------------------------------------------------------------------------------+

 [ Client ]                                                            [ Server ]
 (Browser/CLI)                                                    (Nginx / Spring Boot)
      |                                                                    |
      | 1. DNS Query: "api.shortener.com" -> IP (1.2.3.4)                 |
      |------------------------------------------------------------------->|
      |                                                                    |
      | 2. TCP 3-Way Handshake (SYN -> SYN-ACK -> ACK)                      |
      |<==================================================================>|
      |                                                                    |
      | 3. TLS 1.3 Handshake (Key Exchange, Certificate Check)             |
      |<==================================================================>|
      |                                                                    |
      | 4. HTTP Request: GET /api/v1/s/8xK2qP                              |
      |------------------------------------------------------------------->|
      |                                                                    |
      | 5. Business Logic, Cache Lookup (Redis), DB Fetch (MySQL)          |
      |    =========================================================>      |
      |                                                                    |
      | 6. HTTP Response: 302 Found (Location: https://example.com/target)|
      |<-------------------------------------------------------------------|
```

#### Detailed Breakdown of the Step Chain:
1. **DNS Lookup**: Maps domain `api.shortener.com` to IP address `1.2.3.4` (Recursively through Browser Cache -> OS Cache -> Router Cache -> ISP Resolver -> Root -> TLD -> Authoritative DNS).
2. **TCP Connection**: Establishes a reliable transmission channel via 3-way handshake (`SYN`, `SYN-ACK`, `ACK`).
3. **TLS Handshake**: Encrypts payload using TLS 1.3 symmetric keys negotiated via ECDHE.
4. **HTTP Protocol**: Transmits HTTP headers and payload (JSON or Redirect headers).
5. **Server Execution**: Reverse proxy (Nginx) forwards request to Spring Boot app -> Redis lookup -> MySQL fallback.
6. **HTTP Response**: Returns standard status codes (`302 Found` or `301 Moved Permanently`) and headers.

---

### 1.2 Protocol Stack Breakdown: DNS, TCP, HTTP/HTTPS, REST, and JSON

| Technology / Protocol | Problem Solved | What Happens If We Don't Use It? | Industry Alternatives | Primary Trade-Offs |
| :--- | :--- | :--- | :--- | :--- |
| **DNS (Domain Name System)** | Translates human-readable names (`bit.ly`) to machine IP addresses (`185.199.108.153`). | Users would have to remember numerical IPv4/IPv6 addresses. Server IP changes would break all clients. | Static IP mapping, Anycast routing, Host files. | Latency introduced on cache miss; DNS propagation delays (TTL). |
| **TCP (Transmission Control Protocol)** | Ensures ordered, lossless, flow-controlled byte-stream delivery over unreliable IP networks. | Packets arrive out of order, get lost, or corrupt data silently without retransmission. | UDP, QUIC (HTTP/3). | High connection overhead (handshake round trips, congestion window ramp-up). |
| **HTTP / HTTPS** | Application-layer protocol for web resources with end-to-end security via TLS. | Unencrypted plain text readable by MITM (Man-in-the-Middle); no standardized header/verb semantics. | gRPC over HTTP/2, WebSockets, RSocket. | TLS encryption CPU cost (~1-2%), header overhead compared to binary protocols. |
| **REST (Representational State Transfer)** | Architectural style using standard HTTP verbs (GET, POST, PUT, DELETE) and stateless operations. | Ad-hoc RPC endpoints (`/doAction1`), high coupling, lack of HTTP caching compatibility. | GraphQL, gRPC, Thrift. | Over-fetching or under-fetching data; verbose JSON payloads compared to Protobuf. |
| **JSON & Serialization** | Language-agnostic string representation for complex data structures. | Backend cannot communicate with frontend or microservices written in different languages. | Protocol Buffers (Protobuf), Avro, MessagePack. | Text parsing overhead, larger wire size, lack of strict schema enforcement out-of-the-box. |

---

### 1.3 Latency Numbers Every Senior Engineer Must Know (Jeff Dean's Cheat Sheet)

Understanding latency dictates architecture decisions (e.g., when to cache in RAM vs read from SSD vs query across the network).

| Operation | Time (Nanoseconds / Microseconds / Milliseconds) | Human Scale Equivalent (scaled up 1 sec = 1 ns) |
| :--- | :--- | :--- |
| L1 Cache Reference | ~0.5 ns | 0.5 seconds |
| Branch Mispredict | ~5 ns | 5 seconds |
| L2 Cache Reference | ~7 ns | 7 seconds |
| Main Memory (RAM) Reference | ~100 ns | 1.6 minutes |
| Read 1 MB sequentially from Memory | ~250,000 ns (250 µs) | 2.9 days |
| Read 1 MB sequentially from NVMe SSD | ~1,000,000 ns (1 ms) | 11.5 days |
| Read 1 MB sequentially from HDD | ~20,000,000 ns (20 ms) | 231 days |
| **Send packet CA (USA) to NL (Europe) and back (Round Trip)** | **~150,000,000 ns (150 ms)** | **4.7 years** |

> **Key takeaway for our URL Shortener**: Reading short URL mapping from Redis RAM takes **~1 ms**, while querying disk-bound DB across regions takes **~50–150 ms**. Cache is **50x to 150x faster**.

---

### 1.4 High Availability, Scalability, and CAP Theorem

#### Availability vs Scalability
- **Availability ($A$)**: Percentage of time the system remains operational under load or hardware failures (e.g., 99.999% "Five Nines" = ~5.26 minutes downtime/year).
- **Scalability**: Ability of a system to handle increased load without degrading performance.
  - **Vertical Scaling (Scale Up)**: Adding more CPU/RAM to a single machine. *Limit: Hardware caps, single point of failure (SPOF).*
  - **Horizontal Scaling (Scale Out)**: Adding more server instances behind a Load Balancer. *Limit: Operational complexity, data consistency.*

#### CAP Theorem Deep-Dive (Brewer's Theorem)
In a distributed data store, you can only guarantee **two** of the following three properties during a network partition ($P$):

```
                      / \
                     /   \
                    /  C  \
                   /       \
                  / Consistency \
                 /_____________\
                /\             /\
               /  \   CAP     /  \
              /    \ Theorem /    \
             /  A   \       /  P   \
            / Availability \ Partition \
           /________________\ Tolerance \
          /______________________________\
```

1. **Consistency ($C$)**: Every read receives the most recent write or an error.
2. **Availability ($A$)**: Every non-failing node returns a non-error response (without guarantee that it contains the most recent write).
3. **Partition Tolerance ($P$)**: The system continues to operate despite network packet loss or network split between nodes.

> **Crucial Fact**: In real-world distributed networks, **Partition Tolerance ($P$) is mandatory**. Network hardware, cables, and switches WILL fail. Therefore, the real architectural choice is **$CP$ vs $AP$**.

- **$CP$ System (e.g., MongoDB, HBase, Redis Cluster in strict mode)**: During network partition, refuses writes/reads on minority partitions to preserve data consistency.
- **$AP$ System (e.g., Cassandra, DynamoDB, Couchbase)**: During network partition, remains available for writes/reads, sacrificing immediate consistency (achieves **Eventual Consistency**).

#### PACELC Extension to CAP Theorem
CAP only explains behavior *during a partition ($P$)*. What about normal operation (*else ($E$)*)?
- **If Partition ($P$)**: Choose between **Availability ($A$)** and **Consistency ($C$)**.
- **Else ($E$)**: Choose between **Latency ($L$)** and **Consistency ($C$)**.

*URL Shortener Classification*: Read operations for short URLs prioritize **Low Latency** and **High Availability** ($AP$ / $EL$). If a user updates a link title or custom alias, a few milliseconds of lag in global propagation (eventual consistency) is completely acceptable.

---

### 1.5 Caching & Load Balancing Fundamentals

#### Load Balancing Topology
Load balancers distribute incoming traffic across pool of backend instances to prevent any single instance from becoming a bottleneck.

```
                    [ Clients (Global) ]
                             |
                             v
                 [ Anycast DNS / Cloudflare CDN ]
                             |
                             v
                [ Layer 4 Load Balancer (HAProxy / NLB) ]
                             |
                             v
                [ Layer 7 Load Balancer (Nginx / ALB) ]
                /            |            \
               v             v             v
       [ App Node 1 ]  [ App Node 2 ]  [ App Node 3 ]
```

- **Layer 4 (L4) Load Balancing**: Operates at TCP/UDP level. Fast, high throughput, routes based on IP & Port without parsing HTTP body/headers.
- **Layer 7 (L7) Load Balancing**: Operates at Application level. Parses HTTP path, headers, cookies, TLS termination. Enables path-based routing (e.g., `/api/v1/shorten` -> Service A, `/analytics` -> Service B).

#### Load Balancing Algorithms
1. **Round Robin**: Distributes requests sequentially. Good for homogeneous servers.
2. **Weighted Round Robin**: Distributes based on capacity/specifications of nodes.
3. **Least Connections**: Sends requests to node with active lowest connections. Ideal for long-lived connections.
4. **IP Hash / Consistent Hashing**: Hashes client IP or URL key to route to the same server node. Essential for stateful session sticking or cache affinity.

---

## 2. Real-World Engineering Analysis: How Big Tech Solves This

- **Google (Google URL Shortener / Firebase Dynamic Links)**: Uses global Anycast routing (BGP Anycast) to direct DNS requests to the nearest edge PoP (Point of Presence), terminating TLS as close to the user as possible.
- **Bitly**: Uses multi-tier caching (Edge CDN -> L7 Proxy -> In-Memory KV Store -> Relational/NoSQL DB) to achieve sub-10ms redirect responses across billions of requests per month.
- **Netflix**: Applies CAP theorem via $AP$ architecture for user streaming playback (Cassandra/DynamoDB) to ensure video play clicks never fail even during cloud region network splits.

---

## 3. Revision Notes & Key Takeaways

1. **Latency hierarchy**: RAM ($100\text{ ns}$) $\ll$ NVMe SSD ($1\text{ ms}$) $\ll$ Cross-country Network ($150\text{ ms}$).
2. **TCP Handshake overhead**: Every new TCP connection costs $1.5$ round-trip times (RTT). HTTPS adds another $1\text{ RTT}$ (TLS 1.3). Reuse connections via HTTP Keep-Alive / Connection Pooling.
3. **CAP Theorem**: Network partitions are unavoidable ($P$). Choose $CP$ for financial transaction safety or $AP$ for massive scale high-availability redirects.
4. **REST/JSON vs gRPC/Protobuf**: REST/JSON is universal for external public APIs; gRPC/Protobuf is preferred for internal high-throughput microservice communication.

---
