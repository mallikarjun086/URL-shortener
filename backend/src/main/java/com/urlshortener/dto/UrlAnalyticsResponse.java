package com.urlshortener.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlAnalyticsResponse {
    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private Map<String, Long> topCountries;
    private Map<String, Long> deviceBreakdown;
    private Map<String, Long> topBrowsers;
}
