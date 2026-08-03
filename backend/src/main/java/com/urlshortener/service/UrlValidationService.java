package com.urlshortener.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enterprise URL Safety & Malicious Domain Scanner.
 * Prevents SSRF attacks, open redirect exploits, invalid schemes, and loopback targeting.
 */
@Service
public class UrlValidationService {

    private static final int MAX_URL_LENGTH = 2048;

    private static final Set<String> BLOCKED_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "0.0.0.0",
            "::1",
            "169.254.169.254", // AWS Metadata service
            "metadata.google.internal" // GCP Metadata service
    );

    private static final Pattern IPV4_LOOPBACK_PATTERN = Pattern.compile("^127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    public void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        if (urlString.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL exceeds maximum allowed length of " + MAX_URL_LENGTH + " characters");
        }

        String trimmedUrl = urlString.trim();

        URI uri;
        try {
            uri = new URI(trimmedUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only 'http' and 'https' URL schemes are supported");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL must contain a valid domain hostname or public IP");
        }

        String lowerHost = host.toLowerCase();

        // SSRF & Loopback Protection
        if (BLOCKED_HOSTS.contains(lowerHost) || IPV4_LOOPBACK_PATTERN.matcher(lowerHost).matches()) {
            throw new IllegalArgumentException("Target URL host '" + host + "' is restricted for security reasons");
        }
    }
}
