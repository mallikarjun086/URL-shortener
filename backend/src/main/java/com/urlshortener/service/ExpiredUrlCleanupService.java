package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background Service to periodically purge expired URLs from Database and Redis cache.
 */
@Service
@Slf4j
public class ExpiredUrlCleanupService {

    private final UrlMappingRepository urlMappingRepository;
    private final RedisCacheService redisCacheService;

    public ExpiredUrlCleanupService(UrlMappingRepository urlMappingRepository,
                                   RedisCacheService redisCacheService) {
        this.urlMappingRepository = urlMappingRepository;
        this.redisCacheService = redisCacheService;
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    @Transactional
    public void cleanupExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running scheduled cleanup for expired URLs at {}", now);

        List<UrlMapping> expiredMappings = urlMappingRepository.findByExpiresAtBefore(now);
        if (expiredMappings.isEmpty()) {
            log.info("No expired URLs found.");
            return;
        }

        for (UrlMapping mapping : expiredMappings) {
            redisCacheService.evictUrl(mapping.getShortCode());
        }

        urlMappingRepository.deleteAll(expiredMappings);
        log.info("Successfully cleaned up {} expired URLs.", expiredMappings.size());
    }
}
