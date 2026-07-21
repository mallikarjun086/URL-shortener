# Phase 1: Requirements Gathering & Capacity Planning

## 1. Functional Requirements
1. **Short URL Generation**: Given a long URL (e.g. `https://example.com/very/long/path?param=123`), system generates a short unique 7-character alias (e.g. `http://short.ly/8xK2qP`).
2. **Redirection**: Visiting `http://short.ly/8xK2qP` redirects the user to the long URL via HTTP `302 Found`.
3. **Custom Aliases**: Users can optionally specify custom short codes (e.g. `http://short.ly/my-custom-link`).
4. **Link Expiration & TTL**: Optional expiration date for links; expired links return `404 Not Found`.
5. **Analytics & Metrics**: System tracks click count, device type (Mobile/Desktop), country, browser, OS, and timestamp for every redirect.
6. **User Accounts & Auth**: Registration, login (JWT), and user link management dashboard.

---

## 2. Non-Functional Requirements
1. **Ultra-Low Latency**: Redirect lookup must respond in $<10\text{ ms}$ average.
2. **High Availability**: $99.999\%$ uptime ("Five Nines"). Redirection must never be down.
3. **High Scalability**: Read-heavy workload with 100:1 read-to-write ratio.
4. **Collision Resistance**: Short code generation must be 100% collision-free.
5. **Security**: Prevention of Open Redirect vulnerabilities, rate limiting against bot spam, SQL injection prevention.

---

## 3. Back-of-the-Envelope Scale Estimation

### Traffic Scale Assumptions
- **New Short URLs (Writes)**: 10 Million links created / month.
- **Read-to-Write Ratio**: 100 : 1 (1 Billion redirects / month).
- **Write Operations per Second (RPS)**:
  $$\text{Writes/sec} = \frac{10,000,000}{30 \times 86,400} \approx 3.85 \text{ writes/sec} \approx 4 \text{ RPS}$$
  $$\text{Peak Write RPS (2x)} \approx 8 \text{ RPS}$$
- **Read Operations per Second (RPS)**:
  $$\text{Reads/sec} = \frac{1,000,000,000}{30 \times 86,400} \approx 385.8 \text{ reads/sec} \approx 400 \text{ RPS}$$
  $$\text{Peak Read RPS (5x spike)} \approx 2,000 \text{ RPS}$$

### Storage Calculations (5 Years Horizon)
- Total URLs stored in 5 years:
  $$10,000,000 \times 12 \times 5 = 600,000,000 \text{ (600 Million records)}$$
- Average record size:
  - `id`: 8 bytes (BIGINT / Snowflake)
  - `short_code`: 16 bytes (VARCHAR)
  - `original_url`: 512 bytes (VARCHAR)
  - `created_at`: 8 bytes (TIMESTAMP)
  - `expires_at`: 8 bytes (TIMESTAMP)
  - `user_id`: 8 bytes (BIGINT)
  - Overhead / Indexes: 100 bytes
  - **Total per record**: ~660 bytes.
- Total Storage in 5 Years:
  $$600,000,000 \times 660 \text{ bytes} \approx 396 \text{ GB}$$

### Memory / Caching Estimation (80/20 Pareto Rule)
- 20% of hot URLs generate 80% of read traffic.
- Daily read traffic:
  $$\frac{1,000,000,000}{30} \approx 33.3 \text{ Million reads/day}$$
- 20% hot links to cache:
  $$33,333,333 \times 0.20 \approx 6.67 \text{ Million URLs to cache in RAM}$$
- Redis RAM required:
  $$6,670,000 \text{ URLs} \times 600 \text{ bytes} \approx 4 \text{ GB RAM}$$

### Bandwidth Calculations
- **Incoming Write Data**: $4 \text{ writes/sec} \times 512 \text{ bytes} \approx 2 \text{ KB/sec}$.
- **Outgoing Read Data**: $400 \text{ reads/sec} \times 512 \text{ bytes} \approx 204.8 \text{ KB/sec}$.
