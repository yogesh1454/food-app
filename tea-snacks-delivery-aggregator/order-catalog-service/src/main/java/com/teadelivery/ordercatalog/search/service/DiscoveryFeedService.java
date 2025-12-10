package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.DiscoveryFeedResponse;

import java.util.UUID;

/**
 * Discovery Feed Service - Aggregates nearby vendors, popular items, 
 * recommendations, and trending items
 */
public interface DiscoveryFeedService {
    
    /**
     * Get personalized discovery feed with 4 sections:
     * 1. Nearby Vendors
     * 2. Popular Items (last 30 days)
     * 3. Recommended Items (personalized)
     * 4. Top Ordered Items (trending, last 7 days)
     * 
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Search radius in kilometers
     * @param userId User ID for personalization
     * @param page Page number (0-based)
     * @param size Page size
     * @return Discovery feed with 4 sections
     */
    DiscoveryFeedResponse getDiscoveryFeed(
            Double latitude,
            Double longitude,
            Integer radiusKm,
            UUID userId,
            Integer page,
            Integer size
    );
}

