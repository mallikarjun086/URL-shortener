package com.urlshortener.controller;

import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/urls")
public class AnalyticsController {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;

    public AnalyticsController(UrlMappingRepository urlMappingRepository,
                               ClickAnalyticsRepository clickAnalyticsRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.clickAnalyticsRepository = clickAnalyticsRepository;
    }

    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<UrlAnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));

        Map<String, Long> countries = toMap(clickAnalyticsRepository.countByCountryForUrl(urlMapping.getId()));
        Map<String, Long> devices = toMap(clickAnalyticsRepository.countByDeviceForUrl(urlMapping.getId()));
        Map<String, Long> browsers = toMap(clickAnalyticsRepository.countByBrowserForUrl(urlMapping.getId()));

        UrlAnalyticsResponse response = UrlAnalyticsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(urlMapping.getOriginalUrl())
                .totalClicks(urlMapping.getClickCount())
                .topCountries(countries)
                .deviceBreakdown(devices)
                .topBrowsers(browsers)
                .build();

        return ResponseEntity.ok(response);
    }

    private Map<String, Long> toMap(List<Object[]> queryResults) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : queryResults) {
            String key = (row[0] != null) ? row[0].toString() : "Unknown";
            Long count = (Long) row[1];
            result.put(key, count);
        }
        return result;
    }
}
