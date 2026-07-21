package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_analytics", indexes = {
        @Index(name = "idx_analytics_url_id", columnList = "url_id"),
        @Index(name = "idx_analytics_clicked_at", columnList = "clickedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private UrlMapping urlMapping;

    private String ipAddress;
    private String country;
    private String deviceType;
    private String browser;
    private String os;
    private String referrer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime clickedAt;

    @PrePersist
    protected void onCreate() {
        if (this.clickedAt == null) {
            this.clickedAt = LocalDateTime.now();
        }
    }
}
