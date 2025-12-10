package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.SearchRequest;
import com.teadelivery.ordercatalog.search.dto.SearchResponse;
import com.teadelivery.ordercatalog.search.model.SearchAnalytics;
import com.teadelivery.ordercatalog.search.repository.SearchAnalyticsRepository;
import com.teadelivery.ordercatalog.search.service.SearchAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Search Analytics Service Implementation
 * 
 * Asynchronously tracks search queries for analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchAnalyticsServiceImpl implements SearchAnalyticsService {
    
    private final SearchAnalyticsRepository analyticsRepository;
    
    @Override
    @Async
    public void trackSearch(
            SearchRequest request,
            SearchResponse response,
            Long responseTimeMs,
            Boolean cacheHit
    ) {
        try {
            int totalResults = 0;
            if (response.getResults() != null) {
                totalResults = (response.getResults().getVendors() != null ? response.getResults().getVendors().size() : 0) +
                              (response.getResults().getItems() != null ? response.getResults().getItems().size() : 0);
            }
            
            SearchAnalytics analytics = SearchAnalytics.builder()
                    .queryText(request.getQuery())
                    .queryType(request.getType() != null ? request.getType() : "all")
                    .searchContext("unified_search")
                    .userId(null) // TODO: Get from authentication context
                    .sessionId(null) // TODO: Get from session
                    .latitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null)
                    .longitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null)
                    .city(request.getCity())
                    .filters(request.getFilters())
                    .resultCount(totalResults)
                    .responseTimeMs(responseTimeMs != null ? responseTimeMs.intValue() : 0)
                    .cacheHit(cacheHit)
                    .rankingStrategy(response.getMetadata() != null ? response.getMetadata().getRankingStrategy() : null)
                    .build();
            
            analyticsRepository.save(analytics);
            log.debug("Tracked search: query='{}', results={}, time={}ms", 
                    request.getQuery(), totalResults, responseTimeMs);
                    
        } catch (Exception e) {
            log.error("Failed to track search analytics", e);
            // Don't fail the request if analytics tracking fails
        }
    }
    
    @Override
    @Async
    public void trackFeedView(
            UUID userId,
            Double latitude,
            Double longitude,
            Integer resultCount,
            Long responseTimeMs,
            Boolean cacheHit
    ) {
        try {
            SearchAnalytics analytics = SearchAnalytics.builder()
                    .queryText("discovery_feed")
                    .queryType("feed")
                    .searchContext("discovery")
                    .userId(userId)
                    .latitude(latitude != null ? BigDecimal.valueOf(latitude) : null)
                    .longitude(longitude != null ? BigDecimal.valueOf(longitude) : null)
                    .resultCount(resultCount)
                    .responseTimeMs(responseTimeMs != null ? responseTimeMs.intValue() : 0)
                    .cacheHit(cacheHit)
                    .build();
            
            analyticsRepository.save(analytics);
            log.debug("Tracked feed view: userId={}, results={}, time={}ms", 
                    userId, resultCount, responseTimeMs);
                    
        } catch (Exception e) {
            log.error("Failed to track feed analytics", e);
        }
    }
}


