package com.urlshortener.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    @DisplayName("Should generate unique IDs sequentially")
    void testUniqueIdGeneration() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            long id = generator.nextId();
            assertTrue(id > 0, "ID must be a positive 64-bit long integer");
            ids.add(id);
        }

        assertEquals(count, ids.size(), "All generated Snowflake IDs must be unique");
    }
}
