package com.urlshortener.service;

import com.urlshortener.algorithm.SnowflakeIdGenerator;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private KafkaAnalyticsProducerService kafkaProducerService;

    @Mock
    private BloomFilterService bloomFilterService;

    private SnowflakeIdGenerator idGenerator;
    private UrlValidationService urlValidationService;
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        idGenerator = new SnowflakeIdGenerator(1, 1);
        urlValidationService = new UrlValidationService();
        urlShortenerService = new UrlShortenerService(
                idGenerator, urlMappingRepository, userRepository, redisCacheService, kafkaProducerService, bloomFilterService, urlValidationService
        );
    }

    @Test
    @DisplayName("Should successfully shorten URL and cache it in Redis")
    @SuppressWarnings("null")
    void testShortenUrlSuccess() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .longUrl("https://example.com/target-page")
                .build();

        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            return mapping;
        });

        ShortenUrlResponse response = urlShortenerService.shortenUrl(request, null);

        assertNotNull(response);
        assertNotNull(response.getShortCode());
        assertEquals("https://example.com/target-page", response.getOriginalUrl());

        verify(redisCacheService, times(1)).cacheUrl(eq(response.getShortCode()), eq("https://example.com/target-page"));
    }

    @Test
    @DisplayName("Should resolve URL from Redis cache when hit occurs")
    void testResolveUrlFromCache() {
        String shortCode = "8xK2qP";
        String targetUrl = "https://example.com/target-page";

        when(bloomFilterService.mightContain(shortCode)).thenReturn(true);
        when(redisCacheService.getUrl(shortCode)).thenReturn(Optional.of(targetUrl));
        UrlMapping mockMapping = UrlMapping.builder().id(123L).shortCode(shortCode).originalUrl(targetUrl).build();
        when(urlMappingRepository.findByShortCode(shortCode)).thenReturn(Optional.of(mockMapping));

        String resolved = urlShortenerService.resolveUrlAndTrackClick(shortCode, "127.0.0.1", "Mozilla", "direct");

        assertEquals(targetUrl, resolved);
        verify(kafkaProducerService, times(1)).publishClickEvent(eq(123L), eq("127.0.0.1"), eq("Mozilla"), eq("direct"));
    }
}
