package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UrlShortenerControllerTest {

    private final UrlShortenerService urlShortenerService = Mockito.mock(UrlShortenerService.class);
    private final UrlShortenerController controller = new UrlShortenerController(urlShortenerService);

    @Test
    void shortenUrl_Success() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setLongUrl("https://example.com");

        ShortenUrlResponse expectedResponse = ShortenUrlResponse.builder()
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .originalUrl("https://example.com")
                .clickCount(0L)
                .build();

        when(urlShortenerService.shortenUrl(any(ShortenUrlRequest.class), eq(null)))
                .thenReturn(expectedResponse);

        ResponseEntity<ShortenUrlResponse> response = controller.shortenUrl(request, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ShortenUrlResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("abc123", body.getShortCode());
    }

    @Test
    void getUserUrls_AuthenticatedUser() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        ShortenUrlResponse link = ShortenUrlResponse.builder()
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .originalUrl("https://example.com")
                .clickCount(5L)
                .build();

        when(urlShortenerService.getUserUrls("user@example.com"))
                .thenReturn(List.of(link));

        ResponseEntity<List<ShortenUrlResponse>> response = controller.getUserUrls(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ShortenUrlResponse> bodyList = response.getBody();
        assertNotNull(bodyList);
        assertEquals(1, bodyList.size());
        assertEquals("abc123", bodyList.get(0).getShortCode());
    }
}
