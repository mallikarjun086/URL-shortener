package com.urlshortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class KafkaAnalyticsConsumerService {

    private final ObjectMapper objectMapper;
    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final GeoIpResolverService geoIpResolverService;

    public KafkaAnalyticsConsumerService(ObjectMapper objectMapper,
                                         UrlMappingRepository urlMappingRepository,
                                         ClickAnalyticsRepository clickAnalyticsRepository,
                                         GeoIpResolverService geoIpResolverService) {
        this.objectMapper = objectMapper;
        this.urlMappingRepository = urlMappingRepository;
        this.clickAnalyticsRepository = clickAnalyticsRepository;
        this.geoIpResolverService = geoIpResolverService;
    }

    @KafkaListener(topics = KafkaAnalyticsProducerService.CLICK_EVENTS_TOPIC, groupId = "url-shortener-group")
    @Transactional
    public void consumeClickEvent(String message) {
        try {
            KafkaAnalyticsProducerService.ClickEventPayload payload =
                    objectMapper.readValue(message, KafkaAnalyticsProducerService.ClickEventPayload.class);

            UrlMapping urlMapping = urlMappingRepository.findById(Objects.requireNonNull(payload.getUrlId())).orElse(null);
            if (urlMapping == null) {
                log.warn("UrlMapping not found for ID: {}", payload.getUrlId());
                return;
            }

            // Increment atomic counter
            urlMappingRepository.incrementClickCount(urlMapping.getId());

            // Parse simple Device / OS / Browser from User-Agent
            String userAgent = payload.getUserAgent() != null ? payload.getUserAgent() : "";
            String deviceType = userAgent.contains("Mobile") ? "Mobile" : "Desktop";
            String browser = parseBrowser(userAgent);
            String os = parseOS(userAgent);
            String country = geoIpResolverService.resolveCountry(payload.getIpAddress());

            ClickAnalytics analytics = ClickAnalytics.builder()
                    .urlMapping(urlMapping)
                    .ipAddress(payload.getIpAddress())
                    .country(country)
                    .deviceType(deviceType)
                    .browser(browser)
                    .os(os)
                    .referrer(payload.getReferrer())
                    .clickedAt(LocalDateTime.now())
                    .build();

            clickAnalyticsRepository.save(Objects.requireNonNull(analytics));
        } catch (Exception e) {
            log.error("Error consuming click event message: {}", message, e);
        }
    }

    private String parseBrowser(String ua) {
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Edge")) return "Edge";
        return "Unknown";
    }

    private String parseOS(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Macintosh") || ua.contains("Mac OS")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }
}
