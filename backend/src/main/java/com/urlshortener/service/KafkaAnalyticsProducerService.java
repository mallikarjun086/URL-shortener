package com.urlshortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class KafkaAnalyticsProducerService {

    public static final String CLICK_EVENTS_TOPIC = "url-click-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaAnalyticsProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("null")
    public void publishClickEvent(Long urlId, String ipAddress, String userAgent, String referrer) {
        try {
            ClickEventPayload payload = ClickEventPayload.builder()
                    .urlId(urlId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            String jsonString = objectMapper.writeValueAsString(payload);
            String nonNullJson = jsonString != null ? jsonString : "{}";
            kafkaTemplate.send(CLICK_EVENTS_TOPIC, String.valueOf(urlId), nonNullJson);
        } catch (Exception e) {
            log.error("Failed to publish click event to Kafka for urlId: {}", urlId, e);
        }
    }

    @Data
    @Builder
    public static class ClickEventPayload {
        private Long urlId;
        private String ipAddress;
        private String userAgent;
        private String referrer;
        private String timestamp;
    }
}
