# System Engineering & Architecture Glossary

| Term | Definition | Context in URL Shortener |
| :--- | :--- | :--- |
| **Anycast** | Network addressing and routing strategy where a single destination IP address is shared by multiple physical routing nodes. | Routes client requests to the physically closest CDN edge node for lowest network latency. |
| **Base62** | Encoding scheme using 62 alphanumeric characters (`[0-9][a-z][A-Z]`). | Used to convert numerical 64-bit integer IDs into short URL strings (e.g., `12592301` -> `8xK2qP`). |
| **Cache-Aside** | Pattern where the application checks the cache first; if missing, reads from DB and populates cache. | Primary read strategy for fast short URL resolution. |
| **CAP Theorem** | States that a distributed data store can only simultaneously provide 2 of 3 guarantees: Consistency, Availability, Partition Tolerance. | URL Shortener adopts $AP$ (Availability + Partition Tolerance) for read endpoints. |
| **Circuit Breaker** | Design pattern used to detect failures and encapsulate the logic of preventing a failure from constantly recurring during temporary maintenance or outages. | Prevents cascading failures when downstream services (e.g., analytics DB) slow down. |
| **DNS TTL** | Time To Live for DNS record caching. | Controls how long clients and resolvers cache IP mappings. |
| **Idempotency** | Property of an API operation where making multiple identical requests has the same effect as a single request. | `PUT` and `DELETE` API endpoints must be idempotent to handle network retries safely. |
| **L7 Load Balancer** | Load balancer operating at the Application Layer (HTTP/HTTPS). | Routes `/api/v1/shorten` vs `/{shortCode}` to specific microservices or handlers. |
| **Latency** | Time taken for a request to travel from client to server and return a response. | Core KPI target for redirect path is $<10\text{ ms}$. |
| **Rate Limiting** | Controlling the rate of incoming requests from clients to prevent abuse or DDoS attacks. | Prevents automated bots from spamming URL creation endpoints. |
| **TLS 1.3** | Latest cryptographic protocol providing security over computer networks with a faster 1-RTT handshake. | Secures user authentication and link management dashboard. |
