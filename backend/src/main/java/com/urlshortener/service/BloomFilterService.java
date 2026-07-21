package com.urlshortener.service;

import com.urlshortener.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.BitSet;

/**
 * High-Performance Pure Java Bloom Filter for O(1) Cache Penetration Protection.
 */
@Service
@Slf4j
public class BloomFilterService {

    private static final int DEFAULT_SIZE = 2_000_000; // Bit array size
    private static final int NUM_HASH_FUNCTIONS = 5;

    private final BitSet bitSet = new BitSet(DEFAULT_SIZE);
    private final UrlMappingRepository urlMappingRepository;

    public BloomFilterService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @PostConstruct
    public void init() {
        try {
            urlMappingRepository.findAll().forEach(mapping -> {
                if (mapping.getShortCode() != null) {
                    add(mapping.getShortCode());
                }
            });
            log.info("Pure Java BloomFilter initialized successfully.");
        } catch (Exception e) {
            log.warn("BloomFilter initial DB load warning: {}", e.getMessage());
        }
    }

    public synchronized void add(String value) {
        if (value == null) return;
        int[] hashes = getHashes(value);
        for (int hash : hashes) {
            bitSet.set(Math.abs(hash % DEFAULT_SIZE));
        }
    }

    public synchronized boolean mightContain(String value) {
        if (value == null) return false;
        int[] hashes = getHashes(value);
        for (int hash : hashes) {
            if (!bitSet.get(Math.abs(hash % DEFAULT_SIZE))) {
                return false;
            }
        }
        return true;
    }

    private int[] getHashes(String value) {
        int[] hashes = new int[NUM_HASH_FUNCTIONS];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
                hashes[i] = ((bytes[i * 2] & 0xFF) << 8) | (bytes[i * 2 + 1] & 0xFF);
            }
        } catch (Exception e) {
            int h = value.hashCode();
            for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
                hashes[i] = h + i * 31;
            }
        }
        return hashes;
    }
}
