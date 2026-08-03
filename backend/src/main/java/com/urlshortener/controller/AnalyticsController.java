package com.urlshortener.controller;

import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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

    @GetMapping("/{shortCode}/analytics/export")
    public ResponseEntity<byte[]> exportAnalytics(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "csv") String format) {

        UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));

        Map<String, Long> countries = toMap(clickAnalyticsRepository.countByCountryForUrl(urlMapping.getId()));
        Map<String, Long> devices = toMap(clickAnalyticsRepository.countByDeviceForUrl(urlMapping.getId()));
        Map<String, Long> browsers = toMap(clickAnalyticsRepository.countByBrowserForUrl(urlMapping.getId()));

        byte[] fileBytes;
        String fileName;
        MediaType mediaType;

        if ("json".equalsIgnoreCase(format)) {
            fileName = "analytics-" + shortCode + ".json";
            mediaType = MediaType.APPLICATION_JSON;

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"shortCode\": \"").append(shortCode).append("\",\n");
            json.append("  \"originalUrl\": \"").append(urlMapping.getOriginalUrl()).append("\",\n");
            json.append("  \"totalClicks\": ").append(urlMapping.getClickCount()).append(",\n");
            json.append("  \"topCountries\": ").append(mapToJson(countries)).append(",\n");
            json.append("  \"deviceBreakdown\": ").append(mapToJson(devices)).append(",\n");
            json.append("  \"topBrowsers\": ").append(mapToJson(browsers)).append("\n");
            json.append("}");

            fileBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            fileName = "analytics-" + shortCode + ".csv";
            mediaType = MediaType.parseMediaType("text/csv");

            StringBuilder csv = new StringBuilder();
            csv.append("Category,Item,ClickCount\n");
            csv.append("Summary,Total Clicks,").append(urlMapping.getClickCount()).append("\n");

            countries.forEach((country, count) -> csv.append("Country,\"").append(country).append("\",").append(count).append("\n"));
            devices.forEach((device, count) -> csv.append("Device,\"").append(device).append("\",").append(count).append("\n"));
            browsers.forEach((browser, count) -> csv.append("Browser,\"").append(browser).append("\",").append(count).append("\n"));

            fileBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType != null ? mediaType : MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
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

    private String mapToJson(Map<String, Long> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
