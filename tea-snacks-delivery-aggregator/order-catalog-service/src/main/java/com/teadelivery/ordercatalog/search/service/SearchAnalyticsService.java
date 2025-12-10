package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.SearchRequest;
import com.teadelivery.ordercatalog.search.dto.SearchResponse;

import java.util.UUID;

/**
 * Search Analytics Service
 * 
 * Tracks search queries for analytics, optimization, and insights
 */
public interface SearchAnalyticsService {
    
    /**
     * Track search query execution
     * 
     * @param request Search request
     * @param response Search response
     * @param responseTimeMs Response time in milliseconds
     * @param cacheHit Was result served from cache
     */
    void trackSearch(
            SearchRequest request,
            SearchResponse response,
            Long responseTimeMs,
            Boolean cacheHit
    );
    
    /**
     * Track discovery feed view
     * 
     * @param userId User ID (if authenticated)
     * @param latitude User latitude
     * @param longitude User longitude
     * @param resultCount Number of results
     * @param responseTimeMs Response time
     * @param cacheHit Cache hit status
     */
    void trackFeedView(
            UUID userId,
            Double latitude,
            Double longitude,
            Integer resultCount,
            Long responseTimeMs,
            Boolean cacheHit
    );
}


