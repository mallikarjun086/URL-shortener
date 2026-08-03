package com.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidationServiceTest {

    private UrlValidationService urlValidationService;

    @BeforeEach
    void setUp() {
        urlValidationService = new UrlValidationService();
    }

    @Test
    @DisplayName("Valid HTTP & HTTPS URLs should pass validation")
    void testValidUrls() {
        assertDoesNotThrow(() -> urlValidationService.validateUrl("https://example.com/page"));
        assertDoesNotThrow(() -> urlValidationService.validateUrl("http://google.com/search?q=test"));
        assertDoesNotThrow(() -> urlValidationService.validateUrl("https://sub.domain.co.uk/path/to/resource#section"));
    }

    @Test
    @DisplayName("Empty or null URLs should throw IllegalArgumentException")
    void testEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl(null));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("   "));
    }

    @Test
    @DisplayName("Disallowed schemes should throw IllegalArgumentException")
    void testDisallowedSchemes() {
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("data:text/html,<script>alert(1)</script>"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("ftp://fileserver.com/doc"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("file:///C:/Windows/System32"));
    }

    @Test
    @DisplayName("SSRF target loopback hostnames and metadata IPs should throw IllegalArgumentException")
    void testSsrfProtection() {
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("http://localhost/admin"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("http://127.0.0.1:8080/metrics"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("http://127.0.0.2/secret"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("http://169.254.169.254/latest/meta-data/"));
        assertThrows(IllegalArgumentException.class, () -> urlValidationService.validateUrl("http://metadata.google.internal/computeMetadata/v1/"));
    }
}
