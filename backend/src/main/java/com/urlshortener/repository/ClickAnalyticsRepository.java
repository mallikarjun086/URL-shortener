package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {
    List<ClickAnalytics> findByUrlMappingId(Long urlMappingId);

    @Query("SELECT c.country, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.id = :urlId GROUP BY c.country")
    List<Object[]> countByCountryForUrl(@Param("urlId") Long urlId);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.id = :urlId GROUP BY c.deviceType")
    List<Object[]> countByDeviceForUrl(@Param("urlId") Long urlId);

    @Query("SELECT c.browser, COUNT(c) FROM ClickAnalytics c WHERE c.urlMapping.id = :urlId GROUP BY c.browser")
    List<Object[]> countByBrowserForUrl(@Param("urlId") Long urlId);
}
