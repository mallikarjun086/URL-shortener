package com.urlshortener.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    @DisplayName("Should correctly encode and decode numbers back to original value")
    void testEncodeAndDecode() {
        long original = 12592301L;
        String encoded = Base62Encoder.encode(original);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());

        long decoded = Base62Encoder.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("Should handle 0 correctly")
    void testZero() {
        assertEquals("0", Base62Encoder.encode(0));
        assertEquals(0, Base62Encoder.decode("0"));
    }

    @Test
    @DisplayName("Should throw exception for invalid characters during decode")
    void testInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("invalid-char!@#"));
    }
}
