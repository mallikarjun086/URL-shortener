package com.urlshortener.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "dGhpc0lzQVNlY3JldEtleUZvckpXVENyZWF0aW9uQW5kVmFsaWRhdGlvbkluT3VyVVJMU2hvcnRlbmVyQXBwbGljYXRpb24xMjM0NTY3ODkw";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, 3600000);
    }

    @Test
    void generateAndValidateToken_Success() {
        String email = "test@example.com";
        String role = "ROLE_USER";

        String token = jwtTokenProvider.generateToken(email, role);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }
}
