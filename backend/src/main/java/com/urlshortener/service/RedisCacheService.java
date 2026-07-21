package com.urlshortener.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
public class RedisCacheService {

    private static final String URL_KEY_PREFIX = "url:shortCode:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> getUrl(String shortCode) {
        try {
            String value = redisTemplate.opsForValue().get(URL_KEY_PREFIX + shortCode);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            // Degrade gracefully if Redis fails
            return Optional.empty();
        }
    }

    public void cacheUrl(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(
                    URL_KEY_PREFIX + shortCode, 
                    Objects.requireNonNull(originalUrl), 
                    Objects.requireNonNull(DEFAULT_TTL)
            );
        } catch (Exception ignored) {
            // Fail open if Redis write encounters network blip
        }
    }

    public void evictUrl(String shortCode) {
        try {
            redisTemplate.delete(URL_KEY_PREFIX + shortCode);
        } catch (Exception ignored) {
        }
    }
}
