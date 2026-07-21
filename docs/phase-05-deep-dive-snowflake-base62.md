# Phase 05: Deep Dive into ID Generation (Twitter Snowflake) & Base62 Encoding

## 1. Overview

In high-concurrency distributed URL shorteners, generating unique, compact, collision-free identifiers is critical. Traditional methods like database `AUTO_INCREMENT` or random MD5/UUID hash generation suffer from scaling bottlenecks or hash collisions.

This application uses a two-tier approach:
1. **Twitter Snowflake Algorithm** ([SnowflakeIdGenerator.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/algorithm/SnowflakeIdGenerator.java)): Generates globally unique, time-ordered 64-bit integer IDs.
2. **Base62 Encoding** ([Base62Encoder.java](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/src/main/java/com/urlshortener/algorithm/Base62Encoder.java)): Converts the 64-bit integer into a compact URL-safe 7-character string.

---

## 2. Twitter Snowflake 64-bit ID Structure

```
 1 bit   41 bits                      5 bits      5 bits      12 bits
+------+----------------------------+-----------+-----------+------------------+
| Sign | Timestamp (Epoch Offset)   | DataCenter| Worker ID | Sequence Number  |
| (0)  | in Milliseconds            | ID        |           | (0 - 4095 / ms)  |
+------+----------------------------+-----------+-----------+------------------+
```

### Bit Distribution:
- **Sign Bit (1 bit)**: Always `0` for positive long integers in Java.
- **Timestamp Bits (41 bits)**: Stores milliseconds elapsed since custom epoch (`2024-01-01 00:00:00 UTC`).
  - Total lifespan: $2^{41} \text{ ms} \approx 69 \text{ years}$.
- **Datacenter ID (5 bits)**: Up to 32 data centers ($2^5$).
- **Worker Node ID (5 bits)**: Up to 32 application server instances per data center ($2^5$).
- **Sequence Number (12 bits)**: Allows up to 4,096 IDs ($2^{12}$) per millisecond per node.

### Concurrency Capacity:
$$\text{Max Throughput per Node} = 4,096 \text{ IDs/ms} = 4,096,000 \text{ IDs/sec}$$

---

## 3. Base62 Character Encoding

Base62 uses URL-safe alphanumeric characters:
$$\text{Base62 Alphabet} = \text{"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"}$$

### Mathematical Capacity:
For a 7-character short URL code:
$$\text{Capacity} = 62^7 = 3,521,614,606,208 \text{ unique combinations} \approx 3.52 \text{ Trillion URLs}$$

### Algorithm Implementation:

```java
public static String encode(long number) {
    if (number == 0) return "0";
    StringBuilder sb = new StringBuilder();
    long num = Math.abs(number);
    while (num > 0) {
        int remainder = (int) (num % 62);
        sb.append(BASE62_CHARS.charAt(remainder));
        num /= 62;
    }
    return sb.reverse().toString();
}
```

---

## 4. Advantages over Alternative Approaches

| Feature | Auto-Increment DB | Random MD5/SHA256 | **Snowflake + Base62** |
| :--- | :--- | :--- | :--- |
| **Collision Risk** | None | High (Requires DB retry loop) | **Zero (Guaranteed unique)** |
| **Database Overhead** | High (Primary DB lock) | High (Lookups on write) | **Zero (In-memory generation)** |
| **Security** | Weak (Sequential `/1`, `/2`) | Strong | **Strong (Non-predictable)** |
| **Time Ordering** | Yes | No | **Yes (Index friendly)** |
