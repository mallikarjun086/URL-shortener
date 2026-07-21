# Phase 3: Database Schema & ER Diagram Design

## ER Diagram (Relational Model)

```
+------------------------+        +-----------------------------------+
|         users          |        |           url_mappings            |
+------------------------+        +-----------------------------------+
| PK id          BIGINT  |<-------| PK id               BIGINT        |
|    email       VARCHAR | 1    N | FK user_id          BIGINT        |
|    password    VARCHAR |        |    short_code       VARCHAR(16)   | (UNIQUE INDEX)
|    role        VARCHAR |        |    original_url     TEXT          |
|    created_at  DATETIME|        |    created_at       DATETIME      |
+------------------------+        |    expires_at       DATETIME      |
                                  |    click_count      BIGINT        |
                                  +-----------------------------------+
                                                    | 1
                                                    |
                                                    | N
                                  +-----------------------------------+
                                  |          click_analytics          |
                                  +-----------------------------------+
                                  | PK id               BIGINT        |
                                  | FK url_id           BIGINT        |
                                  |    ip_address       VARCHAR(45)   |
                                  |    country          VARCHAR(64)   |
                                  |    device_type      VARCHAR(32)   |
                                  |    browser          VARCHAR(32)   |
                                  |    os               VARCHAR(32)   |
                                  |    referrer         VARCHAR(255)  |
                                  |    clicked_at       DATETIME      |
                                  +-----------------------------------+
```

---

## DDL Scripts (MySQL 8 Dialect)

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE url_mappings (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    short_code VARCHAR(16) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    click_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_short_code (short_code),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE click_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    country VARCHAR(64),
    device_type VARCHAR(32),
    browser VARCHAR(32),
    os VARCHAR(32),
    referrer VARCHAR(255),
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (url_id) REFERENCES url_mappings(id) ON DELETE CASCADE,
    INDEX idx_analytics_url_id (url_id),
    INDEX idx_analytics_clicked_at (clicked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
