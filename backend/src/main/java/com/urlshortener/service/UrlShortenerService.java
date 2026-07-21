package com.urlshortener.service;

import com.urlshortener.algorithm.Base62Encoder;
import com.urlshortener.algorithm.SnowflakeIdGenerator;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.entity.User;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UrlShortenerService {

    private final SnowflakeIdGenerator idGenerator;
    private final UrlMappingRepository urlMappingRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;
    private final KafkaAnalyticsProducerService kafkaProducerService;
    private final BloomFilterService bloomFilterService;

    @Value("${app.domain:http://localhost:8080}")
    private String domain;

    public UrlShortenerService(SnowflakeIdGenerator idGenerator,
                               UrlMappingRepository urlMappingRepository,
                               UserRepository userRepository,
                               RedisCacheService redisCacheService,
                               KafkaAnalyticsProducerService kafkaProducerService,
                               BloomFilterService bloomFilterService) {
        this.idGenerator = idGenerator;
        this.urlMappingRepository = urlMappingRepository;
        this.userRepository = userRepository;
        this.redisCacheService = redisCacheService;
        this.kafkaProducerService = kafkaProducerService;
        this.bloomFilterService = bloomFilterService;
    }

    @Transactional
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request, String userEmail) {
        String shortCode;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            shortCode = request.getCustomAlias().trim();
            if (urlMappingRepository.existsByShortCode(shortCode)) {
                throw new IllegalArgumentException("Custom alias '" + shortCode + "' is already in use");
            }
        } else {
            long uniqueId = idGenerator.nextId();
            shortCode = Base62Encoder.encode(uniqueId);
        }

        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        LocalDateTime expiresAt = null;
        if (request.getExpiresInDays() != null && request.getExpiresInDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpiresInDays());
        }

        long id = idGenerator.nextId();
        UrlMapping urlMapping = UrlMapping.builder()
                .id(id)
                .shortCode(shortCode)
                .originalUrl(request.getLongUrl())
                .user(user)
                .clickCount(0L)
                .expiresAt(expiresAt)
                .build();

        urlMappingRepository.save(Objects.requireNonNull(urlMapping));

        // Register with Bloom Filter to prevent cache penetration
        bloomFilterService.add(shortCode);

        // Pre-populate Redis cache for sub-10ms lookup on first hit
        redisCacheService.cacheUrl(shortCode, request.getLongUrl());

        return ShortenUrlResponse.builder()
                .shortCode(shortCode)
                .shortUrl(domain + "/" + shortCode)
                .originalUrl(request.getLongUrl())
                .createdAt(urlMapping.getCreatedAt())
                .expiresAt(expiresAt)
                .clickCount(0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ShortenUrlResponse> getUserUrls(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return List.of();
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<UrlMapping> mappings = urlMappingRepository.findByUserId(user.getId());

        return mappings.stream()
                .map(urlMapping -> ShortenUrlResponse.builder()
                        .shortCode(urlMapping.getShortCode())
                        .shortUrl(domain + "/" + urlMapping.getShortCode())
                        .originalUrl(urlMapping.getOriginalUrl())
                        .createdAt(urlMapping.getCreatedAt())
                        .expiresAt(urlMapping.getExpiresAt())
                        .clickCount(urlMapping.getClickCount())
                        .build())
                .collect(Collectors.toList());
    }

    public String resolveUrlAndTrackClick(String shortCode, String ipAddress, String userAgent, String referrer) {
        // 0. Bloom Filter Check (O(1) Cache Penetration Defense)
        if (!bloomFilterService.mightContain(shortCode)) {
            throw new IllegalArgumentException("Short code not found: " + shortCode);
        }

        // 1. Redis Cache Lookup (Sub-1ms)
        Optional<String> cachedUrl = redisCacheService.getUrl(shortCode);
        if (cachedUrl.isPresent()) {
            // Asynchronous analytics tracking via Kafka
            UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode).orElse(null);
            if (mapping != null) {
                kafkaProducerService.publishClickEvent(mapping.getId(), ipAddress, userAgent, referrer);
            }
            return cachedUrl.get();
        }

        // 2. Database Fallback Lookup
        UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));

        if (urlMapping.getExpiresAt() != null && urlMapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Short URL has expired");
        }

        // Populate Redis cache for subsequent calls
        redisCacheService.cacheUrl(shortCode, urlMapping.getOriginalUrl());

        // Asynchronous analytics tracking via Kafka
        kafkaProducerService.publishClickEvent(urlMapping.getId(), ipAddress, userAgent, referrer);

        return urlMapping.getOriginalUrl();
    }
}
