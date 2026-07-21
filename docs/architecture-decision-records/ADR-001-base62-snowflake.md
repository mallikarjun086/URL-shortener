# ADR-001: Short URL Generation Strategy - Snowflake ID + Base62 Encoding

- **Status**: Accepted
- **Deciders**: Staff Engineer & Architect
- **Date**: 2026-07-19

---

## 1. Context & Problem Statement
We need an algorithm to generate unique, short, human-readable 7-character strings for incoming URLs at high write volume without hash collisions or database locking overhead.

---

## 2. Options Evaluated

1. **MD5 / SHA256 Truncation**: Hash long URL with MD5 and take first 7 characters.
   - *Problem*: High probability of hash collisions ($2^{42}$ keys space). Requires database query check-and-retry logic on every write.
2. **Auto-Incrementing Database ID + Base62**: Use MySQL `AUTO_INCREMENT` integer ID and encode to Base62.
   - *Problem*: Single database bottleneck for ID generation; predictable sequential URLs (security risk: attacker can scrape links sequentially `/1`, `/2`, `/3`).
3. **UUID (v4) + Base62**: Generate 128-bit random UUID and encode.
   - *Problem*: String length exceeds 7 characters (~22 chars). Sub-optimal index performance on non-sequential keys.
4. **Twitter Snowflake Distributed ID Generator + Base62 (Chosen)**:
   - Generate a 64-bit time-ordered integer ID using 41-bit timestamp, 10-bit worker node ID, 12-bit sequence number. Encode this 64-bit integer into Base62.

---

## 3. Calculation & Character Capacity
Base62 uses `[0-9][a-z][A-Z]` (62 unique characters).
- 6-character code capacity: $62^6 \approx 56.8 \text{ Billion unique URLs}$
- 7-character code capacity: $62^7 \approx 3.5 \text{ Trillion unique URLs}$

Using 7 characters easily covers our 5-year requirement of 600 Million URLs.

---

## 4. Decision
We select **Twitter Snowflake Distributed ID + Base62 Encoding**. This provides:
1. Zero collision risk across distributed application nodes without database locks.
2. High index efficiency (time-ordered sequential 64-bit integers).
3. Cryptographically non-predictable short codes when mixed with worker ID offsets.
